package am.devvibes.buyandsell.dto.description;

import am.devvibes.buyandsell.dto.field.FieldDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DescriptionDto {

	private String header_eng;
	private String header_ru;
	private String header_hy;
	private List<FieldDto> fields;

}
