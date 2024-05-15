package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.entity.ItemForSell;

import java.util.List;

public interface ItemForSellMapper {

	ItemForSell mapDtoToEntity(ItemForSellRequestDto productRequestDto);

	ItemForSellResponseDto mapEntityToDto(ItemForSell productEntity);

	List<ItemForSellResponseDto> mapEntityListToDtoList(List<ItemForSell> productEntities);

}
