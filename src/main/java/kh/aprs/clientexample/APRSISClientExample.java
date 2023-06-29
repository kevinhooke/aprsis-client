package kh.aprs.clientexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class APRSISClientExample {

	public String buildLogonString(String callsign, String aprsISPassword) {
		StringBuilder sb = new StringBuilder();
		sb.append("user ");
		sb.append(callsign);
		sb.append(" pass ");
		sb.append(aprsISPassword);
		sb.append(" vers kk6dct-aprs-client 0.1 filter r/38.55/-121.73/50");
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
		}
	}	
}

