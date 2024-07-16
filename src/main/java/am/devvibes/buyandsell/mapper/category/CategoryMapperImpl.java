package am.devvibes.buyandsell.mapper.category;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.dto.description.DescriptionDto;
import am.devvibes.buyandsell.dto.field.FieldDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.description.DescriptionEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.service.measurement.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryMapperImpl implements CategoryMapper {

	private final MeasurementService measurementService;

	@Override
	public CategoryDto mapToDto(CategoryEntity category) {

		return CategoryDto.builder()
				.name(category.getName())
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
						.header(descriptionEntity.getHeader())
						.fields(mapFieldsToDto(descriptionEntity.getFields()))
						.build())
				.toList();
	}

	private List<FieldDto> mapFieldsToDto(List<FieldNameEntity> fields) {
		return fields.stream()
				.map(fieldEntity -> FieldDto.builder()
						.fieldName(fieldEntity.getFieldName())
						.value(fieldEntity.getValue())
						.measurement(Objects.nonNull(fieldEntity.getMeasurement()) ?
								fieldEntity.getMeasurement().getSymbol() : null)
						.build())
				.toList();
	}

}
