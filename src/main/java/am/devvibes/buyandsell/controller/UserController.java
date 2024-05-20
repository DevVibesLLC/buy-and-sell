package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserServiceImpl userService;

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
		UserResponseDto userResponseDto = userService.findUserById(id);
		return ResponseEntity.ok(userResponseDto);
	}

	@GetMapping
	public ResponseEntity<List<UserResponseDto>> getAllUsers() {
		List<UserResponseDto> allUsers = userService.findAllUsers();
		return ResponseEntity.ok(allUsers);
	}

	@DeleteMapping("{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable String id) {
		userService.deleteUser(id);
		return ResponseEntity.ok().build();
	}

}