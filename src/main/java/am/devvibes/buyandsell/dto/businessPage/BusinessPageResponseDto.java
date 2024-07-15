package am.devvibes.buyandsell.dto.businessPage;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.location.Location;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessPageResponseDto {

	private Long id;

	private String ownerId;

	private String bannerKey;

	private String logoKey;

	private String title;

	private String description;

	private Location location;

	private Long categoryId;

	private String email;

	private List<String> phoneNumbers;

	private Map<String, String> workingDaysAndHours;

	private Map<String, String> socialMediaLinks;

	private List<ItemResponseDto> adds;

}
