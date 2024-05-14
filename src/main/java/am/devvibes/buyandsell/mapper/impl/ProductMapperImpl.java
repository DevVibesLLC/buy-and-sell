package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.mapper.ProductMapper;
import am.devvibes.buyandsell.model.dto.product.ProductRequestDto;
import am.devvibes.buyandsell.model.dto.product.ProductResponseDto;
import am.devvibes.buyandsell.model.entity.ItemForSell;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductMapperImpl implements ProductMapper {

	@Override
	public ItemForSell mapDtoToEntity(ProductRequestDto productRequestDto) {
		return ItemForSell.builder()
				.name(productRequestDto.getName())
				.description(productRequestDto.getDescription())
				.category(productRequestDto.getCategory())
				.price(productRequestDto.getPrice())
				.quantity(productRequestDto.getQuantity())
				.build();
	}

	@Override
	public ProductResponseDto mapEntityToDto(ItemForSell productEntity) {
		return ProductResponseDto.builder()
				.id(productEntity.getId())
				.name(productEntity.getName())
				.description(productEntity.getDescription())
				.category(productEntity.getCategory())
				.price(productEntity.getPrice())
				.quantity(productEntity.getQuantity())
				.createdAt(productEntity.getCreatedAt())
				.updatedAt(productEntity.getUpdatedAt())
				.build();
	}

	@Override
	public List<ProductResponseDto> mapEntityListToDtoList(List<ItemForSell> productEntities) {
		List<ProductResponseDto> productResponseDtoList = new ArrayList<>();
		for (ItemForSell productEntity : productEntities) {
			ProductResponseDto productResponseDto = mapEntityToDto(productEntity);
			productResponseDtoList.add(productResponseDto);
		}
		return productResponseDtoList;
	}

}
