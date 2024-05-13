package am.devvibes.buyandsell.model.dto;

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
	private Boolean isAccountNonExpired;
	private Boolean isAccountNonLocked;
	private Boolean isCredentialsNonExpired;
	private Boolean isEnabled;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}