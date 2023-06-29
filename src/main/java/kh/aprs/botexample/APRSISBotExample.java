package kh.aprs.botexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class APRSISBotExample {

	public String buildLogonString(String callsign, String aprsISPassword) {
		StringBuilder sb = new StringBuilder();
		sb.append("user ");
		sb.append(callsign);
		sb.append(" pass ");
		sb.append(aprsISPassword);
		//sb.append(" vers kk6dct-aprs-client 0.1 filter r/38.55/-121.73/50");
		sb.append(" vers kk6dct-aprs-client 0.1 filter g/DCTTEST");
		return sb.toString();
	}
	
	public void parseAprsIsPackets(String aprsIsServername, String callsign, String aprsISPassword) throws UnknownHostException, IOException {
		Socket socket = new Socket(aprsIsServername, 14580);
		PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader sockerReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		socketWriter.println(this.buildLogonString(callsign, aprsISPassword));
		
		
		while (true) {
			String aprsPacket = sockerReader.readLine();
			System.out.println(aprsPacket);
			if(aprsPacket.charAt(0) != '#') {
				int ackCount = this.parseMessage(socketWriter, aprsPacket);
				
				if(ackCount > 0) {
					//send ack for received message
				}
			}
		}
	}
	
	/**
	 * Example message: KK6DCT-7>APDR16,TCPIP*,qAC,T2BIO::DCTTEST  :Test 5{5
	 * 
	 * @param socketWriter
	 * @param aprsMessage
	 * @return
	 */
	int parseMessage(PrintWriter socketWriter, String aprsMessage) {
		//split message on '>' to get sender callsign
		String[] messageParts = aprsMessage.split(">");
		String senderCallsign = messageParts[0];
		System.out.println(senderCallsign);
		
		int ackCount = 0;
		
		//find last occurrence of '{' that represents an ack request for the message
		int lastIndexOfAckRequest = aprsMessage.lastIndexOf("{");
		if(lastIndexOfAckRequest > 0) {
			String ackNumber = aprsMessage.substring(lastIndexOfAckRequest + 1);
			System.out.println("Ack number is: " + ackNumber);
			this.sendAck(socketWriter, senderCallsign, ackNumber);
		}
		
		//send reply
		String reply = "DCTTEST>APRS,TCPIP*::" + senderCallsign + ":reply at " + new Date();
		System.out.println("Sending response: " + reply);
		socketWriter.println(reply);
		
		return ackCount;
	}

	void sendAck(PrintWriter socketWriter, String senderCallsign, String ackNumber) {
		//TODO extract callsign and pad to 9 chars
		String ack = "DCTTEST>APRS::" + senderCallsign + ":ack" + ackNumber;
		System.out.println("Sending ack response: " + ack);
		socketWriter.println(ack);
	}
	
}
