package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.autoMark.AutoMarkDto;
import am.devvibes.buyandsell.dto.autoModel.AutoModelDto;
import am.devvibes.buyandsell.dto.generation.GenerationDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import am.devvibes.buyandsell.service.category.ConstCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/const")
public class ConstController {

	private final ConstCategoryService constCategoryService;

	@GetMapping("/category/{categoryId}/marks")
	public ResponseEntity<List<AutoMarkDto>> getAllMarks(@PathVariable Long categoryId) {
		List<AutoMarkDto> marksByCategory = constCategoryService.findMarksByCategory(categoryId);
		return ResponseEntity.ok(marksByCategory);
	}

	@GetMapping("/mark/{markId}/models")
	public ResponseEntity<List<AutoModelDto>> getAllModels(@PathVariable Long markId) {
		List<AutoModelDto> modelsByMark = constCategoryService.findModelsByMark(markId);
		return ResponseEntity.ok(modelsByMark);
	}

	@GetMapping("/model/{modelId}/generations")
	public ResponseEntity<List<GenerationDto>> getAllGenerations(@PathVariable Long modelId) {
		List<GenerationDto> generationsByModel = constCategoryService.findGenerationsByModel(modelId);
		return ResponseEntity.ok(generationsByModel);
	}
}
