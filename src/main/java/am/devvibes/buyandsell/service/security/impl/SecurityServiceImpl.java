package am.devvibes.buyandsell.service.security.impl;

import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.UserEntity;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {



	public String getCurrentUserId() {
		Jwt principal =
				(Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal.getSubject();
	}

}
