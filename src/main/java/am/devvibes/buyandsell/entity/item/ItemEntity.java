package am.devvibes.buyandsell.entity.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.entity.location.Location;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
	@JoinColumn(name = "user_id")
	private UserEntity userEntity;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@ManyToOne
	@JoinColumn(name = "business_page_id")
	private BusinessPageEntity businessPage;

	@OneToMany( cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	private List<FieldEntity> fields;

	@ElementCollection
	private List<String> imgKeys;

	@Enumerated(EnumType.STRING)
	private Status status;

	@ElementCollection
	private List<String> phoneNumbers;

}