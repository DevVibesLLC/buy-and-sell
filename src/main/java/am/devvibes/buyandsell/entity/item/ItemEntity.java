package am.devvibes.buyandsell.entity.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.city.CityEntity;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.entity.priceHistory.PriceHistoryEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemEntity extends BaseEntityWithDates {

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String description;

	@Embedded
	@Column(nullable = false)
	private Price price;

	@ManyToOne
	@JoinColumn(name = "city_id", nullable = false)
	private CityEntity city;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private Double lat;

	@Column(nullable = false)
	private Double lon;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity userEntity;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@ManyToOne
	@JoinColumn(name = "business_page_id")
	private BusinessPageEntity businessPage;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	private List<FieldEntity> fields;

	@ElementCollection
	private List<String> imgKeys;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@ElementCollection
	@Column(nullable = false)
	private List<String> phoneNumbers;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
	private List<PriceHistoryEntity> priceHistories;

	private Long countOfViews;

	@ElementCollection
	private List<String> viewedUsersId;

}