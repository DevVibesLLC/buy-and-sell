package am.devvibes.buyandsell.service.configuration;

import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.mapper.impl.UserMapperImpl;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.UserService;
import am.devvibes.buyandsell.service.impl.UserServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class UserTestConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserMapper userMapper(PasswordEncoder passwordEncoder) {
		return new UserMapperImpl(passwordEncoder);
	}

	@Bean
	public UserService userService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
		return new UserServiceImpl(userRepository, userMapper, passwordEncoder);
	}

}
