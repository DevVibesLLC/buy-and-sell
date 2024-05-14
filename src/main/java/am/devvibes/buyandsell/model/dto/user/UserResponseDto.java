package am.devvibes.buyandsell.model.dto.user;

import am.devvibes.buyandsell.model.entity.RoleEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDto {

	private Long id;
	private String email;
	private String name;
	private String secondName;
	private String verificationCode;
	private Boolean isVerified;
	private Boolean isAccountNonExpired;
	private Boolean isAccountNonLocked;
	private Boolean isCredentialsNonExpired;
	private Boolean isEnabled;
	private RoleEntity roleEntity;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}