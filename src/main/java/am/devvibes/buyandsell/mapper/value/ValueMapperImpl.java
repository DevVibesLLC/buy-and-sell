package am.devvibes.buyandsell.mapper.value;

import am.devvibes.buyandsell.dto.field.FieldNameDto;
import am.devvibes.buyandsell.dto.field.FieldValueDto;
import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.field.FieldNameRepository;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValueMapperImpl implements ValueMapper{

	private final FieldNameRepository fieldRepository;

	@Override
	public FieldEntity mapDtoToEntity(FieldValuesDto fieldValuesDto) {
		FieldNameEntity fieldNameEntity = fieldRepository.findById(fieldValuesDto.getFieldId())
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.FIELD_NOT_FOUND));
		return FieldEntity.builder()
				.fieldName(fieldNameEntity)
				.fieldValue_eng(fieldValuesDto.getFieldValues().getFieldValue_eng())
				.fieldValue_ru(fieldValuesDto.getFieldValues().getFieldValue_ru())
				.fieldValue_hy(fieldValuesDto.getFieldValues().getFieldValue_hy())
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
				.fieldNames(FieldNameDto.builder()
						.fieldName_eng(valueEntity.getFieldName().getFieldName_eng())
						.fieldName_ru(valueEntity.getFieldName().getFieldName_ru())
						.fieldName_hy(valueEntity.getFieldName().getFieldName_hy())
						.build())
				.fieldValues(FieldValueDto.builder()
						.fieldValue_eng(valueEntity.getFieldValue_eng())
						.fieldValue_ru(valueEntity.getFieldValue_ru())
						.fieldValue_hy(valueEntity.getFieldValue_hy())
						.build())
				.build();
	}

	@Override
	public List<FieldValuesDto> mapEntityListToDtoList(List<FieldEntity> valueEntities) {
		return valueEntities.stream().map(this::mapEntityToDto).toList();
	}

}
