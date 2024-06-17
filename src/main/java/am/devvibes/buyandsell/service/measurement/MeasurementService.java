package am.devvibes.buyandsell.service.measurement;

import am.devvibes.buyandsell.entity.measurement.MeasurementEntity;

import java.util.List;

public interface MeasurementService {

	MeasurementEntity addMeasurement(String symbol, String category);

	MeasurementEntity findMeasurementById(Long id);

	List<MeasurementEntity> findAllMeasurements();

	void deleteMeasurementById(Long id);

}
