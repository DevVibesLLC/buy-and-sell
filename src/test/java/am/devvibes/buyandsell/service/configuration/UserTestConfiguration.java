package am.devvibes.buyandsell.service.configuration;

import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.email.EmailService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class UserTestConfiguration {

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.clientId}")
	private String clientId;

	@Value("${keycloak.clientSecret}")
	private String clientSecret;

	@Value("${keycloak.domain}")
	private String domain;

	@Bean
	public Keycloak keycloak(){

		return KeycloakBuilder.builder()
				.serverUrl(domain)
				.realm(realm)
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
				.clientId(clientId)
				.clientSecret(clientSecret)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}



	@Bean
	public JavaMailSender javaMailSender() {
		return new JavaMailSenderImpl();
	}

	@Bean
	public EmailService emailService(JavaMailSender mailSender) {
		return new EmailService(mailSender);
	}

	@Bean
	public UserService userService(UserRepository userRepository,
			UserMapper userMapper,
		SecurityService securityService,
			Keycloak keycloak) {
		return new UserServiceImpl(userRepository, userMapper, keycloak, securityService);
	}

}
