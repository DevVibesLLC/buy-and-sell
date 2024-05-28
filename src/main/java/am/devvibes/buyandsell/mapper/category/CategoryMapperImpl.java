package am.devvibes.buyandsell.mapper.category;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.dto.description.DescriptionRequestDto;
import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.DescriptionEntity;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.entity.MeasurementEntity;
import am.devvibes.buyandsell.service.measurement.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

	private List<DescriptionRequestDto> mapDescriptionsToDto(List<DescriptionEntity> descriptions) {

		return descriptions.stream().map(descriptionEntity -> DescriptionRequestDto.builder()
				.header(descriptionEntity.getHeader())
				.fields(mapFieldsToDto(descriptionEntity.getFields()))
				.build()).toList();
	}

	private List<FieldRequestDto> mapFieldsToDto(List<FieldEntity> fields) {
		return fields.stream().map(fieldEntity -> FieldRequestDto.builder()
				.fieldName(fieldEntity.getFieldName())
				.measurement(fieldEntity.getMeasurement().getSymbol())
				.build()).toList();
	}

}
