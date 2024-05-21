package am.devvibes.buyandsell.service.security.impl;

import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

	private final UserService userService;

	@Override
	@Transactional
	public UserResponseDto getCurrentUser() {
		return userService.findUserById(getCurrentUserId());
	}

	private String getCurrentUserId() {
		KeycloakPrincipal<?> principal =
				(KeycloakPrincipal<?>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		AccessToken token = principal.getKeycloakSecurityContext().getToken();
		return token.getSubject();
	}

}
