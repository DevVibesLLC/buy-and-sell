package am.devvibes.buyandsell.mapper.category;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;

import java.util.List;

public interface CategoryMapper {

	CategoryDto mapToDto(CategoryEntity category);

	List<CategoryDto> mapEntityListToDtoList(List<CategoryEntity> category);

}
