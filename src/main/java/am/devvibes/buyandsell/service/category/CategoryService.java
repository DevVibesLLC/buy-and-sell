package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.entity.CategoryEntity;

import java.util.List;

public interface CategoryService {

	CategoryEntity addCategory(String category);

	List<CategoryEntity> findAllCategories();

	CategoryEntity findCategoryById(Long id);

	void deleteCategoryById(Long id);

}
