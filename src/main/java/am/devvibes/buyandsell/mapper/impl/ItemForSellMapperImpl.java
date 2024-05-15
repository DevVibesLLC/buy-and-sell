package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.mapper.ItemForSellMapper;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.entity.ItemForSell;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemForSellMapperImpl implements ItemForSellMapper {

	@Override
	public ItemForSell mapDtoToEntity(ItemForSellRequestDto itemForSellRequestDto) {
		return ItemForSell.builder()
				.name(itemForSellRequestDto.getName())
				.description(itemForSellRequestDto.getDescription())
				.category(itemForSellRequestDto.getCategory())
				.price(itemForSellRequestDto.getPrice())
				.quantity(itemForSellRequestDto.getQuantity())
				.build();
	}

	@Override
	public ItemForSellResponseDto mapEntityToDto(ItemForSell itemForSellEntity) {
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
	public List<ItemForSellResponseDto> mapEntityListToDtoList(List<ItemForSell> itemForSellEntities) {
		List<ItemForSellResponseDto> itemForSellResponseDtoList = new ArrayList<>();
		for (ItemForSell itemForSellEntity : itemForSellEntities) {
			ItemForSellResponseDto itemForSellResponseDto = mapEntityToDto(itemForSellEntity);
			itemForSellResponseDtoList.add(itemForSellResponseDto);
		}
		return itemForSellResponseDtoList;
	}

}
