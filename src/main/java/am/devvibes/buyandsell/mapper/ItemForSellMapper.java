package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.entity.ItemForSellEntity;

import java.util.List;

public interface ItemForSellMapper {

	ItemForSellEntity mapDtoToEntity(ItemForSellRequestDto productRequestDto);

	ItemForSellResponseDto mapEntityToDto(ItemForSellEntity productEntity);

	List<ItemForSellResponseDto> mapEntityListToDtoList(List<ItemForSellEntity> productEntities);

}
