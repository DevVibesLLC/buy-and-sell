package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.ItemEntity;

import java.util.List;

public interface ItemService {

	ItemResponseDto save(ItemRequestDto itemRequestDto);

	ItemResponseDto findById(Long id);

	List<ItemResponseDto> findAllItems();

	void deleteById(Long id);

}
