package am.devvibes.buyandsell.dto.description;

import am.devvibes.buyandsell.dto.field.FieldDto;
import am.devvibes.buyandsell.util.DescriptionNameEnum;
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

	private DescriptionNameEnum header;
	private List<FieldDto> fields;

}
