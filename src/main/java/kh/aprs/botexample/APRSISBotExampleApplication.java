package kh.aprs.botexample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class APRSISBotExampleApplication implements CommandLineRunner{

	@Bean
	public APRSISBotExample getAPRSISClient() {
		return new APRSISBotExample();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(APRSISBotExampleApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception{
		this.getAPRSISClient().parseAprsIsPackets();
	}
}
