package am.devvibes.buyandsell.dto.city;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityDto {

	private Long id;
	private String name_eng;
	private String name_ru;
	private String name_hy;
	private Long regionId;

}
