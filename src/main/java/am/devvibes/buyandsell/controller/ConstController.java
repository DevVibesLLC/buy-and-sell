package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.service.category.ConstCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/const")
public class ConstController {

	private final ConstCategoryService constCategoryService;

	@GetMapping("/category/auto/marks")
	@Operation(summary = "Get all auto marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllAutoMarks() {
		List<VehicleMarkDto> marksByCategory = constCategoryService.findAutoMarks();
		return ResponseEntity.ok(marksByCategory);
	}

	@GetMapping("/category/truck/marks")
	@Operation(summary = "Get all truck marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllTruckMarks() {
		List<VehicleMarkDto> marksByCategory = constCategoryService.findTruckMarks();
		return ResponseEntity.ok(marksByCategory);
	}

	@GetMapping("/category/bus/marks")
	@Operation(summary = "Get all bus marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllBusMarks() {
		List<VehicleMarkDto> marksByCategory = constCategoryService.findBusMarks();
		return ResponseEntity.ok(marksByCategory);
	}

	@GetMapping("/autoMark/{markId}/models")
	@Operation(summary = "Get all auto models by mark id")
	public ResponseEntity<List<VehicleModelDto>> getAllAutoModels(@PathVariable Long markId) {
		List<VehicleModelDto> modelsByMark = constCategoryService.findAutoModelsByMark(markId);
		return ResponseEntity.ok(modelsByMark);
	}

	@GetMapping("/truckMark/{markId}/models")
	@Operation(summary = "Get all truck models by mark id")
	public ResponseEntity<List<VehicleModelDto>> getAllTruckModels(@PathVariable Long markId) {
		List<VehicleModelDto> modelsByMark = constCategoryService.findTruckModelsByMark(markId);
		return ResponseEntity.ok(modelsByMark);
	}

	@GetMapping("/busMark/{markId}/models")
	@Operation(summary = "Get all bus models by mark id")
	public ResponseEntity<List<VehicleModelDto>> getAllBusModels(@PathVariable Long markId) {
		List<VehicleModelDto> modelsByMark = constCategoryService.findBusModelsByMark(markId);
		return ResponseEntity.ok(modelsByMark);
	}

	@GetMapping("field/{fieldId}")
	@Operation(summary = "Get fields by field name id")
	public ResponseEntity<List<String>> getFieldValues(@PathVariable Long fieldId) {
		return ResponseEntity.ok(constCategoryService.findByFieldNameId(fieldId));
	}

}
