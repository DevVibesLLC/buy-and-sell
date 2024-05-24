package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.classes.Price;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemEntity extends BaseEntityWithDates {

	private String name;

	private String description;

	@Embedded
	private Price price;

	@Embedded
	private Location location;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity userEntity;

	@OneToOne
	private CategoryEntity category;

	private String imgUrl;

}
