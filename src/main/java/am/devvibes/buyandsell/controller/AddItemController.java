package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.mapper.category.CategoryMapper;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.field.FieldService;
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
@RequestMapping("/api/v1/add")
public class AddItemController {

	private final CategoryService categoryService;
	private final CategoryMapper categoryMapper;

	@GetMapping("/{category}/form")
	@Operation(summary = "Get form by category id")
	public ResponseEntity<CategoryDto> getCategory(@PathVariable Long category) {
		CategoryEntity categoryEntity = categoryService.findCategoryById(category);
		return ResponseEntity.ok(categoryMapper.mapToDto(categoryEntity));
	}

}
