package am.devvibes.buyandsell.mapper.category;

import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.entity.CategoryEntity;

public interface CategoryMapper {

	CategoryDto mapToDto(CategoryEntity category);

}
