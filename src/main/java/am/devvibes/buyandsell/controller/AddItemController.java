package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/add")
public class AddItemController {

	private final CategoryService categoryService;

	@GetMapping("/{category}/form")
	public ResponseEntity<CategoryDto> getCategory(@PathVariable Long category) {
		CategoryDto categoryById = categoryService.findCategoryById(category);
		return ResponseEntity.ok(categoryById);
	}

}
