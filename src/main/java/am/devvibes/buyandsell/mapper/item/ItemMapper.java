package am.devvibes.buyandsell.mapper.item;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;

import java.util.List;

public interface ItemMapper {

	ItemEntity mapDtoToEntity(ItemRequestDto itemRequestDto, Long categoryId);

	ItemEntity mapDtoToEntityFromBusiness(ItemRequestDto itemRequestDto, BusinessPageEntity businessPageEntity);

	ItemResponseDto mapEntityToDto(ItemEntity itemEntity);

	List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList);

}
