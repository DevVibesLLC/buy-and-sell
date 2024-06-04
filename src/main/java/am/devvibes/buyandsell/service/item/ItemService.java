package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ItemService {

	ItemResponseDto save(ItemRequestDto itemRequestDto, Long categoryId);

	ItemResponseDto findById(Long id);

	Page<ItemResponseDto> findAllItems(PageRequest pageRequest);

	void deleteById(Long id);

	ItemResponseDto update(ItemRequestDto itemRequestDto, Long categoryId, Long itemId);

}
