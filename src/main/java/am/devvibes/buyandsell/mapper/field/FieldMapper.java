package am.devvibes.buyandsell.mapper.field;

import am.devvibes.buyandsell.dto.field.FieldDto;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;

public interface FieldMapper {

	FieldNameEntity mapDtoToEntity(FieldDto fieldRequestDto);

}
