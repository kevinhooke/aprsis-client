package kh.aprs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class APRSISClientApplication implements CommandLineRunner{

	@Bean
	public APRSISClient getAPRSISClient() {
		return new APRSISClient();
	}
	
	@Value("${aprsIsServername}")
	private String aprsIsServername;
	
	@Value("${callsign}")
	private String callsign;
	
	@Value("${aprs-is-password}")
	private String aprsPassword;
	
	
	public static void main(String[] args) {
		SpringApplication.run(APRSISClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception{
		this.getAPRSISClient().parseAprsIsPackets(this.aprsIsServername, this.callsign, this.aprsPassword);
	}
}
