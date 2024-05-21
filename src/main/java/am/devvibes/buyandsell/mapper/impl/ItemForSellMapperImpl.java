package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.dto.itemForSell.CategoryDto;
import am.devvibes.buyandsell.mapper.ItemForSellMapper;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.entity.ItemForSellEntity;
import am.devvibes.buyandsell.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemForSellMapperImpl implements ItemForSellMapper {

	private final CategoryRepository categoryRepository;
	private final UserMapperImpl userMapper;

	@Override
	public ItemForSellEntity mapDtoToEntity(ItemForSellRequestDto itemForSellRequestDto) {
		return ItemForSellEntity.builder()
				.name(itemForSellRequestDto.getName())
				.description(itemForSellRequestDto.getDescription())
				.category(categoryRepository.findById(itemForSellRequestDto.getCategoryId()).orElseThrow())
				.price(itemForSellRequestDto.getPrice())
				.quantity(itemForSellRequestDto.getQuantity())
				.build();
	}

	@Override
	public ItemForSellResponseDto mapEntityToDto(ItemForSellEntity itemForSellEntity) {
		return ItemForSellResponseDto.builder()
				.id(itemForSellEntity.getId())
				.name(itemForSellEntity.getName())
				.description(itemForSellEntity.getDescription())
				.category(mapCategoryToDto(itemForSellEntity))
				.userId(itemForSellEntity.getUserEntity().getId())
				.price(itemForSellEntity.getPrice())
				.quantity(itemForSellEntity.getQuantity())
				.createdAt(itemForSellEntity.getCreatedAt())
				.updatedAt(itemForSellEntity.getUpdatedAt())
				.build();
	}

	private static CategoryDto mapCategoryToDto(ItemForSellEntity itemForSellEntity) {
		return CategoryDto.builder()
				.id(itemForSellEntity.getCategory().getId())
				.name(itemForSellEntity.getCategory().getCategory())
				.build();
	}

	@Override
	public List<ItemForSellResponseDto> mapEntityListToDtoList(List<ItemForSellEntity> itemForSellEntities) {
		List<ItemForSellResponseDto> itemForSellResponseDtoList = new ArrayList<>();
		for (ItemForSellEntity itemForSellEntity : itemForSellEntities) {
			ItemForSellResponseDto itemForSellResponseDto = mapEntityToDto(itemForSellEntity);
			itemForSellResponseDtoList.add(itemForSellResponseDto);
		}
		return itemForSellResponseDtoList;
	}

}
