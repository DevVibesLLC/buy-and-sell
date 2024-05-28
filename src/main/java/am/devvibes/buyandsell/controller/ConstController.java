package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
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
	public ResponseEntity<List<AutoMarkEntity>> getAllMarks(@PathVariable Long categoryId) {
		List<AutoMarkEntity> marksByCategory = constCategoryService.findMarksByCategory(categoryId);
		return ResponseEntity.ok(marksByCategory);
	}

	@GetMapping("/mark/{markId}/models")
	public ResponseEntity<List<AutoModelEntity>> getAllModels(@PathVariable Long markId) {
		List<AutoModelEntity> modelsByMark = constCategoryService.findModelsByMark(markId);
		return ResponseEntity.ok(modelsByMark);
	}

	@GetMapping("/model/{modelId}/generations")
	public ResponseEntity<List<GenerationEntity>> getAllGenerations(@PathVariable Long modelId) {
		List<GenerationEntity> generationsByModel = constCategoryService.findGenerationsByModel(modelId);
		return ResponseEntity.ok(generationsByModel);
	}
}
