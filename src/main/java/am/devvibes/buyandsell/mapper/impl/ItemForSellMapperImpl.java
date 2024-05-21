package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.mapper.ItemForSellMapper;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.entity.ItemForSellEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemForSellMapperImpl implements ItemForSellMapper {

	@Override
	public ItemForSellEntity mapDtoToEntity(ItemForSellRequestDto itemForSellRequestDto) {
		return ItemForSellEntity.builder()
				.name(itemForSellRequestDto.getName())
				.description(itemForSellRequestDto.getDescription())
				.category(itemForSellRequestDto.getCategory())
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
				.category(itemForSellEntity.getCategory())
				.price(itemForSellEntity.getPrice())
				.quantity(itemForSellEntity.getQuantity())
				.createdAt(itemForSellEntity.getCreatedAt())
				.updatedAt(itemForSellEntity.getUpdatedAt())
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
