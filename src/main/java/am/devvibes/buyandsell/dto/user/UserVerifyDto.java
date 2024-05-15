package am.devvibes.buyandsell.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserVerifyDto {

	@NotBlank
	private final String email;
	@NotBlank
	private final String verificationCode;

}
