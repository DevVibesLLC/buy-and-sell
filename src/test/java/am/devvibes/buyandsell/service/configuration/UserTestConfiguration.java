package am.devvibes.buyandsell.service.configuration;

import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.mapper.impl.UserMapperImpl;
import am.devvibes.buyandsell.repository.RoleRepository;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.email.EmailService;
import am.devvibes.buyandsell.service.user.UserService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class UserTestConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserMapper userMapper(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
		return new UserMapperImpl(passwordEncoder, roleRepository);
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
			PasswordEncoder passwordEncoder,
			EmailService emailService,
			RoleRepository roleRepository) {
		return new UserServiceImpl(userRepository, userMapper, passwordEncoder, emailService, roleRepository);
	}

}
