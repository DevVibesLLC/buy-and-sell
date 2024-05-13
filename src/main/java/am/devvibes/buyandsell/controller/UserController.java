package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.model.dto.UserResponseDto;
import am.devvibes.buyandsell.model.dto.UserSignUpDto;
import am.devvibes.buyandsell.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

	private final UserServiceImpl userService;

	@PostMapping
	public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid UserSignUpDto signUpDto) {
		UserResponseDto savedUser = userService.saveUser(signUpDto);
		return ResponseEntity.ok(savedUser);
	}

	@GetMapping("{id}")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable @Positive Long id) {
		UserResponseDto userById = userService.findUserById(id);
		return ResponseEntity.ok(userById);
	}

	@GetMapping
	public ResponseEntity<List<UserResponseDto>> getAllUsers() {
		List<UserResponseDto> allUsers = userService.findAllUsers();
		return ResponseEntity.ok(allUsers);
	}

}