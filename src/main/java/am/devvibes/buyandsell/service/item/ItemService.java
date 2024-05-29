package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;

import java.util.List;

public interface ItemService {

	ItemResponseDto save(ItemRequestDto itemRequestDto, Long categoryId);

	ItemResponseDto findById(Long id);

	List<ItemResponseDto> findAllItems();

	void deleteById(Long id);

}
