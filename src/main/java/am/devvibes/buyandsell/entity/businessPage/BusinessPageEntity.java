package am.devvibes.buyandsell.entity.businessPage;

import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.city.CityEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessPageEntity extends BaseEntityWithDates {

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private UserEntity owner;

	private String bannerKey;

	private String logoKey;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String description;

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
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@Column(nullable = false)
	private String email;

	@ElementCollection
	@Column(nullable = false)
	private List<String> phoneNumbers;

	@ElementCollection
	@Column(nullable = false)
	private Map<String, String> workingDaysAndHours;

	@ElementCollection
	private Map<String, String> socialMediaLinks;

	@OneToMany
	@JoinColumn(name = "business_page_id")
	private List<ItemEntity> adds;

}