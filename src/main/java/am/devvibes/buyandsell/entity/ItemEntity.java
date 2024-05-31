package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemEntity extends BaseEntityWithDates implements Serializable {

	private String title;

	private String description;

	@Embedded
	private Price price;

	@Embedded
	private Location location;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity userEntity;

	@ManyToOne
	private CategoryEntity category;

	@OneToMany
	@JoinColumn(name = "item_id")
	private List<ValueEntity> values;

	private String imgUrl;

}
