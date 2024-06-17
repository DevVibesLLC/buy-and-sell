package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.service.category.CategoryService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/categories")
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping("/{category}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<CategoryEntity> createCategory(@PathVariable @NotBlank String category) {
		//CategoryEntity savedCategory = categoryService.addCategory();
		return ResponseEntity.ok(null);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<CategoryDto> findCategoryById(@PathVariable @Positive Long id) {
		CategoryDto categoryEntity = categoryService.findCategoryById(id);
		return ResponseEntity.ok(categoryEntity);
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<CategoryEntity>> findAllCategories() {
		List<CategoryEntity> categories = categoryService.findAllCategories();
		return ResponseEntity.ok(categories);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Void> deleteCategoryById(@PathVariable @Positive Long id) {
		categoryService.deleteCategoryById(id);
		return ResponseEntity.ok().build();
	}

}

