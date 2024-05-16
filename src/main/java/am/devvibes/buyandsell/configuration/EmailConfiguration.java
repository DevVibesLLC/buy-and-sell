package am.devvibes.buyandsell.configuration;

import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfiguration {

	@Bean
	public JavaMailSender javaMailSender() {
		return new JavaMailSenderImpl();
	}


}
