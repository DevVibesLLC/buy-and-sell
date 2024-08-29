package am.devvibes.buyandsell.mapper.field;

import am.devvibes.buyandsell.dto.field.FieldDto;
import am.devvibes.buyandsell.dto.measurement.MeasurementRequestDto;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.measurement.MeasurementEntity;
import am.devvibes.buyandsell.service.measurement.MeasurementService;
import am.devvibes.buyandsell.service.measurement.impl.MeasurementServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldMapperImpl implements FieldMapper {

	private final MeasurementService measurementService;

	@Override
	public FieldNameEntity mapDtoToEntity(FieldDto fieldRequestDto) {
		return  null;
	}

}
