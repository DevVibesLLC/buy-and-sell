package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.category.CategoryMapper;
import am.devvibes.buyandsell.repository.CategoryRepository;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.field.FieldService;
import am.devvibes.buyandsell.util.CategoryEnum;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final FieldService fieldService;
	private final CategoryMapper categoryMapper;

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
	public CategoryDto findCategoryById(Long id) {
		CategoryEntity categoryEntity = categoryRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));
		return categoryMapper.mapToDto(categoryEntity);
	}

	@Override
	@Transactional
	public void deleteCategoryById(Long id) {
		categoryRepository.deleteById(id);
	}

	@Override
	public CategoryDto createCategoryRequestDto(String category) {
		return CategoryDto.builder()
				.name(Arrays.stream(CategoryEnum.values())
						.filter(c -> c.getName().equals(category))
						.findFirst()
						.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND)))
				//.descriptions(createDescriptions(category))
				.build();
	}

	/*private List<DescriptionRequestDto> createDescriptions(String category) {

		return switch (category) {
			case "car" -> List.of(
					DescriptionRequestDto.builder()
							.header(DescriptionNameEnum.SPECIFICATIONS)
							.fields(List.of(
									FieldRequestDto.builder()
											.fieldName(fieldService.)
											.measurement()
											.build()))
							.build(),
					DescriptionRequestDto.builder()

							.build());

		};

	}*/
}
