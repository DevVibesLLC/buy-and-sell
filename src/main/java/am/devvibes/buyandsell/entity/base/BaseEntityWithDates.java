package am.devvibes.buyandsell.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntityWithDates {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
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
