package am.devvibes.buyandsell.mapper.value;

import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.field.FieldEntity;

import java.util.List;

public interface ValueMapper {

	FieldEntity mapDtoToEntity(FieldValuesDto fieldValuesDto);

	List<FieldEntity> mapDtoListToEntityList(List<FieldValuesDto> fieldValuesDtos);

	FieldValuesDto mapEntityToDto(FieldEntity valueEntity);

	List<FieldValuesDto> mapEntityListToDtoList(List<FieldEntity> valueEntities);

}
