package am.devvibes.buyandsell.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordDto {

	@NotBlank
	private String email;

	@NotBlank
	private String newPassword;

	@NotBlank
	private String repeatNewPassword;

}
