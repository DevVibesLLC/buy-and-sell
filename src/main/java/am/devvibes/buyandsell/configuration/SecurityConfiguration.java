package am.devvibes.buyandsell.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

	private final UserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/", "/home","/signUp", "/static/**","/actuator/**", "/h2/**", "/css/**", "/js/**", "/json/**", "/images/**, /img/**")
						.permitAll()
						.anyRequest()
						.authenticated()
				)
				.formLogin((form) -> form
						.loginPage("/login")
						.failureUrl("/login-error")
						.defaultSuccessUrl("/dispatch",true)
						.permitAll()
				)
				.logout((logout-> logout.logoutSuccessUrl("/login")
						.permitAll()));

		return http.build();
	}

	@Bean
	public DaoAuthenticationProvider authProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
