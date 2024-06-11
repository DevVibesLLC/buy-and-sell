package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.autoMark.AutoMarkDto;
import am.devvibes.buyandsell.dto.autoModel.AutoModelDto;
import am.devvibes.buyandsell.dto.generation.GenerationDto;
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
	public ResponseEntity<List<AutoMarkDto>> getAllMarks(@PathVariable Long categoryId) {
		List<AutoMarkDto> marksByCategory = constCategoryService.findMarksByCategory(categoryId);
		return ResponseEntity.ok(marksByCategory);
	}

	@GetMapping("/mark/{markId}/models")
	@Operation(summary = "Get all models by mark id")
	public ResponseEntity<List<AutoModelDto>> getAllModels(@PathVariable Long markId) {
		List<AutoModelDto> modelsByMark = constCategoryService.findModelsByMark(markId);
		return ResponseEntity.ok(modelsByMark);
	}

	@GetMapping("/model/{modelId}/generations")
	@Operation(summary = "Get all generations by model id")
	public ResponseEntity<List<GenerationDto>> getAllGenerations(@PathVariable Long modelId) {
		List<GenerationDto> generationsByModel = constCategoryService.findGenerationsByModel(modelId);
		return ResponseEntity.ok(generationsByModel);
	}

	@GetMapping("field/{fieldId}")
	@Operation(summary = "Get fields by field name id")
	public ResponseEntity<List<String>> getFieldValues(@PathVariable Long fieldId) {
		return ResponseEntity.ok(constCategoryService.findByFieldNameId(fieldId));
	}

}
