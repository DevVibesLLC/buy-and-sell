package am.devvibes.buyandsell.service.measurement.impl;

import am.devvibes.buyandsell.entity.MeasurementEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.measurement.MeasurementRepository;
import am.devvibes.buyandsell.service.measurement.MeasurementService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MeasurementServiceImpl implements MeasurementService {

	private final MeasurementRepository measurementRepository;

	@Override
	@Transactional
	public MeasurementEntity addMeasurement(String symbol, String category) {
		return measurementRepository.save(MeasurementEntity.builder().category(category).symbol(symbol).build());
	}

	@Override
	@Transactional
	public MeasurementEntity findMeasurementById(Long id) {
		return measurementRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MEASUREMENT_NOT_FOUND));
	}

	@Override
	@Transactional
	public List<MeasurementEntity> findAllMeasurements() {
		return measurementRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteMeasurementById(Long id) {
		measurementRepository.deleteById(id);
	}

}
