package am.devvibes.buyandsell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BuyAndSellApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuyAndSellApplication.class, args);
	}

}
