package am.devvibes.buyandsell.entity.abstracts;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class AbstractUser {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@Column(name = "email", length = 255)
	private String email;

	@Column(name = "email_constraint", length = 255)
	private String emailConstraint;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified = false;

	@Column(name = "enabled", nullable = false)
	private boolean enabled = false;

	@Column(name = "federation_link", length = 255)
	private String federationLink;

	@Column(name = "first_name", length = 255)
	private String firstName;

	@Column(name = "last_name", length = 255)
	private String lastName;

	@Column(name = "realm_id", length = 255)
	private String realmId;

	@Column(name = "username", length = 255)
	private String username;

	@Column(name = "created_timestamp")
	private Long createdTimestamp;

	@Column(name = "service_account_client_link", length = 255)
	private String serviceAccountClientLink;

	@Column(name = "not_before", nullable = false)
	private int notBefore = 0;

}