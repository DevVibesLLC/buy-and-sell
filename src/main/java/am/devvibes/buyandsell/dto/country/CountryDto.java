package am.devvibes.buyandsell.dto.country;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryDto {

	private Long id;
	private String name_eng;
	private String name_ru;
	private String name_hy;

}