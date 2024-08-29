package am.devvibes.buyandsell.service.measurement;

import am.devvibes.buyandsell.dto.measurement.MeasurementRequestDto;
import am.devvibes.buyandsell.entity.measurement.MeasurementEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.measurement.MeasurementRepository;
import am.devvibes.buyandsell.service.measurement.impl.MeasurementServiceImpl;
import am.devvibes.buyandsell.util.ExceptionConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MeasurementServiceTest {

	@Mock
	private MeasurementRepository measurementRepository;

	@InjectMocks
	private MeasurementServiceImpl measurementService;

	@Test
	void addMeasurement__success() {
		MeasurementRequestDto measurementRequestDto = MeasurementRequestDto.builder()
				.category("Category")
				.symbol_eng("Symbol ENG")
				.symbol_ru("Symbol RU")
				.symbol_hy("Symbol HY")
				.build();

		MeasurementEntity measurementEntity = MeasurementEntity.builder()
				.category("Category")
				.symbol_eng("Symbol ENG")
				.symbol_ru("Symbol RU")
				.symbol_hy("Symbol HY")
				.build();

		when(measurementRepository.save(any(MeasurementEntity.class))).thenReturn(measurementEntity);

		MeasurementEntity result = measurementService.addMeasurement(measurementRequestDto);

		assertNotNull(result);
		assertEquals(measurementEntity, result);

		verify(measurementRepository, times(1)).save(any(MeasurementEntity.class));
	}

	@Test
	void findMeasurementById__success() {
		Long measurementId = 1L;
		MeasurementEntity measurementEntity = MeasurementEntity.builder()
				.category("Category")
				.symbol_eng("Symbol ENG")
				.symbol_ru("Symbol RU")
				.symbol_hy("Symbol HY")
				.build();
		measurementEntity.setId(measurementId);

		when(measurementRepository.findById(measurementId)).thenReturn(Optional.of(measurementEntity));

		MeasurementEntity result = measurementService.findMeasurementById(measurementId);

		assertNotNull(result);

		verify(measurementRepository, times(1)).findById(measurementId);
	}

	@Test
	void findMeasurementById__measurementNotFound() {
		Long measurementId = 1L;

		when(measurementRepository.findById(measurementId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> measurementService.findMeasurementById(measurementId), ExceptionConstants.MEASUREMENT_NOT_FOUND.getString());

		verify(measurementRepository, times(1)).findById(measurementId);
	}

	@Test
	void findAllMeasurements__success() {

		MeasurementEntity measurementEntity1 = MeasurementEntity.builder()
				.category("Category1")
				.symbol_eng("Symbol ENG1")
				.symbol_ru("Symbol RU1")
				.symbol_hy("Symbol HY1")
				.build();

		MeasurementEntity measurementEntity2 = MeasurementEntity.builder()
				.category("Category2")
				.symbol_eng("Symbol ENG2")
				.symbol_ru("Symbol RU2")
				.symbol_hy("Symbol HY2")
				.build();

		List<MeasurementEntity> measurementEntities = List.of(measurementEntity1, measurementEntity2);

		when(measurementRepository.findAll()).thenReturn(measurementEntities);

		List<MeasurementEntity> allMeasurements = measurementService.findAllMeasurements();

		assertNotNull(allMeasurements);
		assertEquals(2, allMeasurements.size());

		verify(measurementRepository, times(1)).findAll();
	}


	@Test
	void deleteMeasurementById__success() {

		Long existingId = 1L;

		when(measurementRepository.existsById(existingId)).thenReturn(true);

		measurementService.deleteMeasurementById(existingId);

		verify(measurementRepository, times(1)).existsById(existingId);
		verify(measurementRepository, times(1)).deleteById(existingId);

	}

	@Test
	void deleteMeasurementById__measurementDoesntExist() {

		Long nonExistingId = 1L;

		when(measurementRepository.existsById(nonExistingId)).thenReturn(false);

		measurementService.deleteMeasurementById(nonExistingId);

		verify(measurementRepository, times(1)).existsById(nonExistingId);
		verify(measurementRepository, times(0)).deleteById(nonExistingId);

	}

}
