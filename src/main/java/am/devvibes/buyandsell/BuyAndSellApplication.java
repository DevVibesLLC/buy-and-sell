package am.devvibes.buyandsell;

import am.devvibes.buyandsell.util.LocationEnum;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class BuyAndSellApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuyAndSellApplication.class, args);
	}

}
