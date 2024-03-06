package kh.aprs.botexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

/**
 * APRS bot that listens for messages addressed to the SSID configured in the properties file.
 * 
 * @author kevinhooke
 *
 */
@Component
public class APRSISBotExample {

	@Value("${aprsIsServername}")
	private String aprsIsServername;
	
	@Value("${callsign}")
	private String callsign;
	
	@Value("${aprs-is-password}")
	private String aprsPassword;

	@Value("${bot-ssid}")
	private String botSSID;

	@Value("${lambda-name-sunspots}")
	private String lambdaFunctionName;

	
	public String buildLogonString(String callsign, String aprsISPassword) {
		StringBuilder sb = new StringBuilder();
		sb.append("user ");
		sb.append(callsign);
		sb.append(" pass ");
		sb.append(aprsISPassword);
		//sb.append(" vers kk6dct-aprs-client 0.1 filter r/38.55/-121.73/50");
		sb.append(" vers kk6dct-aprs-client 0.1 filter g/").append(this.botSSID);
		return sb.toString();
	}
	
	public void parseAprsIsPackets() throws UnknownHostException, IOException {
		Socket socket = new Socket(this.aprsIsServername, 14580);
		PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader sockerReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		socketWriter.println(this.buildLogonString(this.callsign, this.aprsPassword));
		
		
		while (true) {
			String aprsPacket = sockerReader.readLine();
			System.out.println(aprsPacket);
			//if not a comment message from the server
			if(aprsPacket.charAt(0) != '#') {
				this.parseMessage(socketWriter, aprsPacket);
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
	void parseMessage(PrintWriter socketWriter, String aprsMessage) {
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
		String sunspotNumber = this.invokeSunspotLambda();
		
		String reply = this.botSSID + ">APRS,TCPIP*::" + senderCallsign + ":Current sunspot count = " + sunspotNumber;
		
		System.out.println("Sending response: " + reply);
		socketWriter.println(reply);
	}

	void sendAck(PrintWriter socketWriter, String senderCallsign, String ackNumber) {
		//TODO extract callsign and pad to 9 chars
		String ack = this.botSSID + ">APRS::" + senderCallsign + ":ack" + ackNumber;
		System.out.println("Sending ack response: " + ack);
		socketWriter.println(ack);
	}
	
	/**
	 * Note: Lambda Handler classes are from:
	 * <pre>
	 * <groupId>com.amazonaws</groupId>
     * <artifactId>aws-java-sdk-lambda</artifactId>
	 * </pre>
	 * 
	 * InvokeRquest apis are from:
	 * <pre>
	 * <groupId>software.amazon.awssdk</groupId>
	 * <artifactId>lambda</artifactId>
	 * </pre>
	 * @return
	 */
	private String invokeSunspotLambda() {
		InvokeResponse res = null;
		String result = null;
		
        try {

        	Region region = Region.EU_WEST_2;
        	LambdaClient lambdaClient = LambdaClient.builder()
        	        .region(region)
        	        .build();
        	
        	//Need a SdkBytes instance for the payload
            String json = "{}";
            SdkBytes payload = SdkBytes.fromUtf8String(json) ;

            //Setup an InvokeRequest
            InvokeRequest request = InvokeRequest.builder()
                    .functionName(lambdaFunctionName)
                    .payload(payload)
                    .build();

            res = lambdaClient.invoke(request);
            result = res.payload().asUtf8String() ;
            System.out.println(result);

        } catch(LambdaException e) {
            System.err.println(e.getMessage());
        }
        
        return result;
	}
}

