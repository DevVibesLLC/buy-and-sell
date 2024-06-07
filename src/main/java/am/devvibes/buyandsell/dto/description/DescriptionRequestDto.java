package am.devvibes.buyandsell.dto.description;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.util.DescriptionNameEnum;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DescriptionRequestDto {

	private DescriptionNameEnum header;
	private List<FieldRequestDto> fields;

}
