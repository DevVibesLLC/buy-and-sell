package am.devvibes.buyandsell.mapper.item;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.ItemEntity;

import java.util.List;

public interface ItemMapper {
	ItemEntity mapDtoToEntity(ItemRequestDto itemRequestDto, Long categoryId);

	ItemResponseDto mapEntityToDto(ItemEntity itemEntity);

	ItemEntity updateEntity(ItemEntity itemEntity, ItemRequestDto updatedEntity);

	List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList);
}
