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
public class LocationCountryDto {

	@Column(nullable = false)
	private String country_eng;
	@Column(nullable = false)
	private String country_ru;
	@Column(nullable = false)
	private String country_hy;

}
