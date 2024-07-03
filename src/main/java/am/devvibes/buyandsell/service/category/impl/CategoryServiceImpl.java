package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.category.CategoryMapper;
import am.devvibes.buyandsell.repository.category.CategoryRepository;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.field.FieldService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final FieldService fieldService;
	private final CategoryMapper categoryMapper;

	@Override
	@Transactional
	public CategoryEntity addCategory(String category) {
		//todo save method isn't working
		return categoryRepository.save(CategoryEntity.builder().build());
	}

	@Override
	@Transactional
	public List<CategoryEntity> findAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	@Transactional
	public CategoryEntity FindCategoryEntityOrElseThrow(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));
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
