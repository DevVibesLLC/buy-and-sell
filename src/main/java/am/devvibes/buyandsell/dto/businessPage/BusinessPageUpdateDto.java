package am.devvibes.buyandsell.dto.businessPage;

import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.location.Location;
import am.devvibes.buyandsell.entity.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessPageUpdateDto {

	private String title;

	private String description;

	private Long cityId;

	private String address;

	private CategoryEntity category;

	private String email;

	private List<String> phoneNumbers;

	private Map<String, String> workingDaysAndHours;

	private Map<String, String> socialMediaLinks;

}
