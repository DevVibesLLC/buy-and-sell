package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.ItemEntity;

import java.util.List;

public interface ItemMapper {
	ItemEntity mapDtoToEntity(ItemRequestDto itemRequestDto);

	ItemResponseDto mapEntityToDto(ItemEntity itemEntity);

	List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList);
}
