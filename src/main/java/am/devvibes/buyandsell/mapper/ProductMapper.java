package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.model.dto.product.ProductRequestDto;
import am.devvibes.buyandsell.model.dto.product.ProductResponseDto;
import am.devvibes.buyandsell.model.entity.ItemForSell;

import java.util.List;

public interface ProductMapper {

	ItemForSell mapDtoToEntity(ProductRequestDto productRequestDto);

	ProductResponseDto mapEntityToDto(ItemForSell productEntity);

	List<ProductResponseDto> mapEntityListToDtoList(List<ItemForSell> productEntities);

}
