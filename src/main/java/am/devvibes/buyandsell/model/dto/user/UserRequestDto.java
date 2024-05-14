package am.devvibes.buyandsell.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDto {

	@NotBlank
	private String email;
	@NotBlank
	private String name;
	@NotBlank
	private String secondName;
	@NotBlank
	private String password;
	@NotBlank
	private String repeatPassword;
}
