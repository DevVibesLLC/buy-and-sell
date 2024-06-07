package am.devvibes.buyandsell.mapper.value;

import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.FieldNameEntity;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.FieldRepository;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValueMapperImpl implements ValueMapper{

	private final FieldRepository fieldRepository;

	@Override
	public FieldEntity mapDtoToEntity(FieldValuesDto fieldValuesDto) {
		FieldNameEntity fieldEntity = fieldRepository.findById(fieldValuesDto.getFieldId())
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.FIELD_NOT_FOUND));
		return FieldEntity.builder()
				.fieldName(fieldEntity)
				.fieldValue(fieldValuesDto.getFieldValue())
				.build();
	}

	@Override
	public List<FieldEntity> mapDtoListToEntityList(List<FieldValuesDto> fieldValuesDtos) {
		return fieldValuesDtos.stream().map(this::mapDtoToEntity).toList();
	}

	@Override
	public FieldValuesDto mapEntityToDto(FieldEntity valueEntity) {
		return FieldValuesDto.builder()
				.fieldId(valueEntity.getFieldName().getId())
				.fieldValue(valueEntity.getFieldValue())
				.build();
	}

	@Override
	public List<FieldValuesDto> mapEntityListToDtoList(List<FieldEntity> valueEntities) {
		return valueEntities.stream().map(this::mapEntityToDto).toList();
	}

}
