package am.devvibes.buyandsell.dto.user;

import am.devvibes.buyandsell.entity.RoleEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {

	private String id;
	private String email;
	private String username;
	private String firstName;
	private String lastName;
	private Boolean isVerified;
	private Boolean isAccountNonExpired;
	private Boolean isAccountNonLocked;
	private Boolean isCredentialsNonExpired;
	private Boolean isEnabled;
	private LocalDateTime createdAt;

}