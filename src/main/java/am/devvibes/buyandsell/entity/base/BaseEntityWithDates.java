package am.devvibes.buyandsell.entity.base;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntityWithDates {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private ZonedDateTime createdAt;
	private ZonedDateTime updatedAt;

	@PrePersist
	protected void onRegister() {
		createdAt = ZonedDateTime.now();
		updatedAt = ZonedDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		ZonedDateTime.now();
	}

}
