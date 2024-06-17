package am.devvibes.buyandsell.service.security.impl;

import am.devvibes.buyandsell.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {



	public String getCurrentUserId() {
		Jwt principal =
				(Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal.getSubject();
	}

}
