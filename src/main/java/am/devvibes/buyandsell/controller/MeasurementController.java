package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.entity.MeasurementEntity;
import am.devvibes.buyandsell.service.measurement.MeasurementService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/measurements")
public class MeasurementController {

	private final MeasurementService measurementService;

	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<MeasurementEntity> createMeasurement(@PathVariable @NotBlank String symbol,
			@NotBlank String category) {
		return ResponseEntity.ok(measurementService.addMeasurement(symbol, category));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<MeasurementEntity> getMeasurementById(@PathVariable Long id) {
		return ResponseEntity.ok(measurementService.findMeasurementById(id));
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<MeasurementEntity>> getAllMeasurement() {
		return ResponseEntity.ok(measurementService.findAllMeasurements());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<MeasurementEntity>> deleteMeasurementById(@PathVariable Long id) {
		measurementService.deleteMeasurementById(id);
		return ResponseEntity.ok().build();
	}

}
