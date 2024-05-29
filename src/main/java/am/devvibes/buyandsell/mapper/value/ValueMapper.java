package am.devvibes.buyandsell.mapper.value;

import am.devvibes.buyandsell.dto.FieldValuesDto;
import am.devvibes.buyandsell.entity.ValueEntity;

import java.util.List;

public interface ValueMapper {

	ValueEntity mapDtoToEntity(FieldValuesDto fieldValuesDto);

	List<ValueEntity> mapDtoListToEntityList(List<FieldValuesDto> fieldValuesDtos);

	FieldValuesDto mapEntityToDto(ValueEntity valueEntity);

	List<FieldValuesDto> mapEntityListToDtoList(List<ValueEntity> valueEntities);

}
