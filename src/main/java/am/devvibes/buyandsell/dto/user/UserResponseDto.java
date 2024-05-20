package am.devvibes.buyandsell.dto.user;

import am.devvibes.buyandsell.entity.RoleEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDto {

	private String id;
	private String email;
	private String username;
	private String firstName;
	private String lastName;
	private String verificationCode;
	private Boolean isVerified;
	private Boolean isAccountNonExpired;
	private Boolean isAccountNonLocked;
	private Boolean isCredentialsNonExpired;
	private Boolean isEnabled;
	private RoleEntity roleEntity;
	private LocalDateTime createdAt;
}