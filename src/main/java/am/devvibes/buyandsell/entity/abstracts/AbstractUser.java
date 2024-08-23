package am.devvibes.buyandsell.entity.abstracts;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

	@Column(name = "email")
	private String email;

	@Column(name = "email_constraint")
	private String emailConstraint;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified = false;

	@Column(name = "enabled", nullable = false)
	private boolean enabled = false;

	@Column(name = "federation_link")
	private String federationLink;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "realm_id")
	private String realmId;

	@Column(name = "username")
	private String username;

	@Column(name = "created_timestamp")
	private Long createdTimestamp;

	@Column(name = "service_account_client_link")
	private String serviceAccountClientLink;

	@Column(name = "not_before", nullable = false)
	private int notBefore = 0;

}