package am.devvibes.buyandsell.service.measurement;

import am.devvibes.buyandsell.dto.measurement.MeasurementRequestDto;
import am.devvibes.buyandsell.entity.measurement.MeasurementEntity;

import java.util.List;

public interface MeasurementService {

	MeasurementEntity addMeasurement(MeasurementRequestDto measurementRequestDto);

	MeasurementEntity findMeasurementById(Long id);

	List<MeasurementEntity> findAllMeasurements();

	void deleteMeasurementById(Long id);

}
