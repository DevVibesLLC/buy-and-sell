package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.user.UserLoginDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.dto.user.UserVerifyDto;
import am.devvibes.buyandsell.service.account.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

	private final AccountService accountService;

	@PostMapping
	public ResponseEntity<UserResponseDto> login(@RequestBody @Valid UserLoginDto userLoginDto) {
		UserResponseDto loginUser = accountService.login(userLoginDto.getEmail(), userLoginDto.getPassword());
		return ResponseEntity.ok(loginUser);
	}

	@PostMapping("/verify")
	public ResponseEntity<Void> verifyAccount(@RequestBody @Valid UserVerifyDto userVerifyDto) {
		accountService.verifyAccount(userVerifyDto.getEmail(), userVerifyDto.getVerificationCode());
		return ResponseEntity.ok().build();
	}

}
