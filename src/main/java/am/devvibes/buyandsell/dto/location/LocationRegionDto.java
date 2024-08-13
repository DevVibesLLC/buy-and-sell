package am.devvibes.buyandsell.dto.location;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class LocationRegionDto {

	@Column(nullable = false)
	private String region_eng;
	@Column(nullable = false)
	private String region_ru;
	@Column(nullable = false)
	private String region_hy;

}
