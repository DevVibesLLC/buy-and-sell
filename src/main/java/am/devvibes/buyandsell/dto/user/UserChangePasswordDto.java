package am.devvibes.buyandsell.dto.user;

import lombok.Data;

@Data
public class UserChangePasswordDto {
	private String email;
	private String newPassword;
	private String repeatNewPassword;
}
