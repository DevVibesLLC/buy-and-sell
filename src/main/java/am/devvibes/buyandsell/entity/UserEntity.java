package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.abstracts.AbstractUser;
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
@Table(name = "user_entity")
public class UserEntity extends AbstractUser {

	public UserEntity(String id,
			String email,
			String emailConstraint,
			boolean emailVerified,
			boolean enabled,
			String federationLink,
			String firstName,
			String lastName,
			String realmId,
			String username,
			Long createdTimestamp,
			String serviceAccountClientLink,
			int notBefore) {
		super(id, email, emailConstraint, emailVerified, enabled, federationLink, firstName, lastName, realmId,
				username, createdTimestamp, serviceAccountClientLink, notBefore);
	}
}