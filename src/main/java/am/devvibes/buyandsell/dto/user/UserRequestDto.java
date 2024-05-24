package am.devvibes.buyandsell.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDto {

	@NotBlank
	private String email;
	@NotBlank
	private String username;
	@NotBlank
	private String firstName;
	@NotBlank
	private String lastName;
	@NotBlank
	private String password;
	@NotBlank
	private String repeatPassword;
}
