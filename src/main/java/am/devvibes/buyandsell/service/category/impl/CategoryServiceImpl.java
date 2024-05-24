package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.CategoryRepository;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	@Override
	@Transactional
	public CategoryEntity addCategory(CategoryEntity categoryEntity) {
		return categoryRepository.save(categoryEntity);
	}

	@Override
	@Transactional
	public List<CategoryEntity> findAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	@Transactional
	public CategoryEntity findCategoryById(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));
	}

	@Override
	@Transactional
	public void deleteCategoryById(Long id) {
		categoryRepository.deleteById(id);
	}

}
