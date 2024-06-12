package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.autoMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.autoModel.VehicleModelDto;
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

	@GetMapping("/category/{categoryId}/marks")
	@Operation(summary = "Get all marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllMarks(@PathVariable Long categoryId) {
		List<VehicleMarkDto> marksByCategory = constCategoryService.findMarksByCategory(categoryId);
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

	@GetMapping("field/{fieldId}")
	@Operation(summary = "Get fields by field name id")
	public ResponseEntity<List<String>> getFieldValues(@PathVariable Long fieldId) {
		return ResponseEntity.ok(constCategoryService.findByFieldNameId(fieldId));
	}

}
