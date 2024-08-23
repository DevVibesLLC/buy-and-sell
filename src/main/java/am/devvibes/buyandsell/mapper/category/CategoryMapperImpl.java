package am.devvibes.buyandsell.mapper.category;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.dto.description.DescriptionDto;
import am.devvibes.buyandsell.dto.field.FieldDto;
import am.devvibes.buyandsell.dto.field.FieldValueDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.description.DescriptionEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.field.FieldValueEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryMapperImpl implements CategoryMapper {

	@Override
	public CategoryDto mapToDto(CategoryEntity category) {

		return CategoryDto.builder()
				.categoryName_eng(category.getName().getEnglish())
				.categoryName_ru(category.getName().getRussian())
				.categoryName_hy(category.getName().getArmenian())
				.descriptions(mapDescriptionsToDto(category.getDescriptions()))
				.build();
	}

	@Override
	public List<CategoryDto> mapEntityListToDtoList(List<CategoryEntity> category) {
		return category.stream().map(this::mapToDto).toList();
	}

	private List<DescriptionDto> mapDescriptionsToDto(List<DescriptionEntity> descriptions) {
		return descriptions.stream()
				.map(descriptionEntity -> DescriptionDto.builder()
						.header_eng(descriptionEntity.getHeader().getEnglish())
						.header_ru(descriptionEntity.getHeader().getRussian())
						.header_hy(descriptionEntity.getHeader().getArmenian())
						.fields(mapFieldsToDto(descriptionEntity.getFields()))
						.build())
				.toList();
	}

	private List<FieldDto> mapFieldsToDto(List<FieldNameEntity> fields) {
		return fields.stream()
				.map(fieldEntity -> FieldDto.builder()
						.fieldName_eng(fieldEntity.getFieldName_eng())
						.fieldName_ru(fieldEntity.getFieldName_ru())
						.fieldName_hy(fieldEntity.getFieldName_hy())
						.value(mapFieldValueToDto(fieldEntity.getValues()))
						.measurement_eng(Objects.nonNull(fieldEntity.getMeasurement()) ?
								fieldEntity.getMeasurement().getSymbol_eng() : null)
						.measurement_ru(Objects.nonNull(fieldEntity.getMeasurement()) ?
								fieldEntity.getMeasurement().getSymbol_ru() : null)
						.measurement_hy(Objects.nonNull(fieldEntity.getMeasurement()) ?
								fieldEntity.getMeasurement().getSymbol_hy() : null)
						.build())
				.toList();
	}

	private List<FieldValueDto> mapFieldValueToDto(List<FieldValueEntity> fieldValues) {
		return fieldValues.stream()
				.map(fieldValue -> FieldValueDto.builder()
						.fieldValue_eng(fieldValue.getValue_eng())
						.fieldValue_ru(fieldValue.getValue_ru())
						.fieldValue_hy(fieldValue.getValue_hy())
						.build())
				.toList();
	}

}
