package am.devvibes.buyandsell.dto.businessPage;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.location.LocationDto;
import jakarta.persistence.Embedded;
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

	private Long categoryId;

	private String ownerId;

	private String title;

	private String description;

	private LocationDto locationDto;

	@Embedded
	private BusinessPageContactsDto contacts;

	private Map<String, String> workingDaysAndHours;

	private String bannerKey;

	private String logoKey;

	private List<ItemResponseDto> adds;

}
