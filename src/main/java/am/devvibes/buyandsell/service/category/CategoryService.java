package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.entity.category.CategoryEntity;

import java.util.List;

public interface CategoryService {

	List<CategoryEntity> findAllCategories();

	CategoryEntity findCategoryById(Long id);

}