package am.devvibes.buyandsell.dto.businessPage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
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
	private Double lat;

	@NotNull
	private Double lon;

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
