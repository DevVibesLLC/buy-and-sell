package am.devvibes.buyandsell.dto.field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldDto {

	private String fieldName_eng;
	private String fieldName_ru;
	private String fieldName_hy;
	private List<FieldValueDto> value;
	private String measurement_eng;
	private String measurement_ru;
	private String measurement_hy;

}
