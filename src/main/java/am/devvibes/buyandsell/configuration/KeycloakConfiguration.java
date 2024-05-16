package am.devvibes.buyandsell.configuration;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfiguration {

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


}
