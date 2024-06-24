package am.devvibes.buyandsell.mapper.item;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemMapper {
	ItemEntity mapDtoToEntity(ItemRequestDto itemRequestDto, List<MultipartFile> images, Long categoryId);

	ItemResponseDto mapEntityToDto(ItemEntity itemEntity);

	List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList);
}
