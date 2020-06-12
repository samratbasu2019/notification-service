package org.infy.mail.batch;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class MailBatchApplicationStarter {

	public static void main(String[] args) {
		SpringApplication.run(MailBatchApplicationStarter.class, args);
	}

}
