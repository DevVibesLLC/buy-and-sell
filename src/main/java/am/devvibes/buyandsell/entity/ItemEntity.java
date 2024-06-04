package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemEntity extends BaseEntityWithDates {

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
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@OneToMany( cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "item_id")
	private List<ValueEntity> values;

	private String imgUrl;

	@Enumerated(EnumType.STRING)
	private Status status;
}
