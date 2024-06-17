package am.devvibes.buyandsell.mapper.field;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;

public interface FieldMapper {

	FieldNameEntity mapDtoToEntity(FieldRequestDto fieldRequestDto);

}
