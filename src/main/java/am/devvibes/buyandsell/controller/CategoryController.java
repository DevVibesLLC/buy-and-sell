package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.mapper.category.CategoryMapper;
import am.devvibes.buyandsell.service.category.CategoryService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/categories")
public class CategoryController {

	private final CategoryService categoryService;
	private final CategoryMapper categoryMapper;

	@PostMapping("/{category}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<CategoryDto> createCategory(@PathVariable @NotBlank String category) {
		CategoryEntity savedCategory = categoryService.addCategory(category);
		return ResponseEntity.ok(categoryMapper.mapToDto(savedCategory));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<CategoryDto> findCategoryById(@PathVariable @Positive Long id) {
		CategoryEntity categoryEntity = categoryService.findCategoryById(id);
		return ResponseEntity.ok(categoryMapper.mapToDto(categoryEntity));
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<CategoryDto>> findAllCategories() {
		List<CategoryEntity> categories = categoryService.findAllCategories();
		return ResponseEntity.ok(categoryMapper.mapEntityListToDtoList(categories));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Void> deleteCategoryById(@PathVariable @Positive Long id) {
		categoryService.deleteCategoryById(id);
		return ResponseEntity.ok().build();
	}

}

