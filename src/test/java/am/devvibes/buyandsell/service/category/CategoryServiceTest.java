package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.category.CategoryRepository;
import am.devvibes.buyandsell.service.category.impl.CategoryServiceImpl;
import am.devvibes.buyandsell.util.CategoryEnum;
import am.devvibes.buyandsell.util.ExceptionConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private CategoryServiceImpl categoryService;

	@Test
	void findAllCategories__success() {

		CategoryEntity categoryEntity1 = CategoryEntity.builder().name(CategoryEnum.CARS).build();
		categoryEntity1.setId(1L);
		CategoryEntity categoryEntity2 = CategoryEntity.builder().name(CategoryEnum.TRUCKS).build();
		categoryEntity1.setId(2L);

		List<CategoryEntity> categoryEntities = Arrays.asList(categoryEntity1, categoryEntity2);

		when(categoryRepository.findAll()).thenReturn(categoryEntities);

		List<CategoryEntity> result = categoryService.findAllCategories();

		assertEquals(result.size(), categoryEntities.size());

		verify(categoryRepository, times(1)).findAll();

	}

	@Test
	void findCategoryById__success() {

		CategoryEntity categoryEntity = CategoryEntity.builder().name(CategoryEnum.CARS).build();
		categoryEntity.setId(1L);

		when(categoryRepository.findById(categoryEntity.getId())).thenReturn(Optional.of(categoryEntity));

		CategoryEntity categoryById = categoryService.findCategoryById(categoryEntity.getId());

		assertEquals(categoryById.getName(), categoryEntity.getName());

		verify(categoryRepository, times(1)).findById(categoryEntity.getId());

	}

	@Test
	void findCategoryById__categoryNotFound() {

		Long categoryId = 1L;

		when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> categoryService.findCategoryById(categoryId), ExceptionConstants.CATEGORY_NOT_FOUND.getString());

		verify(categoryRepository, times(1)).findById(categoryId);

	}

}
