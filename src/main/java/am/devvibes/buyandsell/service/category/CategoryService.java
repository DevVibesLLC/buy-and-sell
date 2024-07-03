package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;

import java.util.List;

public interface CategoryService {

	CategoryEntity addCategory(String category);

	List<CategoryEntity> findAllCategories();

	CategoryEntity FindCategoryEntityOrElseThrow(Long categoryId);

	CategoryEntity findCategoryById(Long id);

	void deleteCategoryById(Long id);

}
