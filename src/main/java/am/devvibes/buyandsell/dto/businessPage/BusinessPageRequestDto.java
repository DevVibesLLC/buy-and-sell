package am.devvibes.buyandsell.dto.businessPage;

import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.location.Location;
import am.devvibes.buyandsell.entity.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessPageRequestDto {

	private String bannerKey;

	private String logoKey;

	@NotBlank
	private String title;

	@NotBlank
	private String description;

	@NotNull
	private Long cityId;

	@NotBlank
	private String address;

	@NotBlank
	private String email;

	@NotNull
	private List<String> phoneNumbers;

	@NotNull
	private Map<String, String> workingDaysAndHours;

	private Map<String, String> socialMediaLinks;

}
