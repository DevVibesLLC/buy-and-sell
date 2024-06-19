package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.dto.filter.*;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ItemService {

	ItemResponseDto save(ItemRequestDto itemRequestDto, Long categoryId);

	ItemResponseDto findById(Long id);

	Page<ItemResponseDto> findAllItems(PageRequest pageRequest);

	void deleteById(Long id);

	ItemResponseDto update(ItemRequestDto itemRequestDto, Long categoryId, Long itemId);

	List<ItemResponseDto> searchItems(SearchDto searchDto);

	List<ItemResponseDto> filterItems(AutoFilterDto filterDto);

	List<ItemResponseDto> filterItems(TruckFilterDto filterDto);

	List<ItemResponseDto> filterItems(BusFilterDto filterDto);

	List<ItemResponseDto> filterItems(ApartmentBuyFilterDto filterDto);

	List<ItemResponseDto> filterItems(ApartmentRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(HouseBuyFilterDto filterDto);

	List<ItemResponseDto> filterItems(HouseRentalFilterDto filterDto);

	List<ItemResponseDto> findItemsByCategory(Long categoryId);

}
