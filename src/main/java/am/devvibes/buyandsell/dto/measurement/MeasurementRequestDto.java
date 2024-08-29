package am.devvibes.buyandsell.dto.measurement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeasurementRequestDto {

	private String symbol_eng;
	private String symbol_ru;
	private String symbol_hy;
	private String category;
}
