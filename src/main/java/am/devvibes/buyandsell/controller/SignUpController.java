package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.mapper.user.UserMapper;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/users/signup")
public class SignUpController {

	private final UserServiceImpl userService;
	private final UserMapper userMapper;

	@PostMapping
	@Operation(summary = "Register user")
	public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid UserRequestDto userRequestDto) {
		UserRepresentation userRepresentation = userService.saveUser(userRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.mapRepresentationToDto(userRepresentation));
	}

}