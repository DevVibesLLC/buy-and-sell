package am.devvibes.buyandsell.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class UserEntity extends AbstractUser {

	@Builder
	public UserEntity(Long id,
			String email,
			String password,
			String name,
			String secondName,
			Boolean isAccountNonExpired,
			Boolean isAccountNonLocked,
			Boolean isCredentialsNonExpired,
			Boolean isEnabled,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super(id, email, password, name, secondName, isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired,
				isEnabled, createdAt, updatedAt);

	}

}