package am.devvibes.buyandsell.mapper.field;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;

public interface FieldMapper {

	FieldEntity mapDtoToEntity(FieldRequestDto fieldRequestDto);

}
