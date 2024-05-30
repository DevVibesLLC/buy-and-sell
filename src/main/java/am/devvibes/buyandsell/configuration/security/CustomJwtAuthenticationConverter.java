package am.devvibes.buyandsell.configuration.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter =
			new JwtGrantedAuthoritiesConverter();

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Collection<GrantedAuthority> authorities = defaultGrantedAuthoritiesConverter.convert(jwt);

		Collection<String> realmRoles = (Collection<String>) jwt.getClaimAsMap("realm_access").get("roles");

		if (realmRoles != null) {
			authorities.addAll(realmRoles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
		}

		return authorities;
	}
}
