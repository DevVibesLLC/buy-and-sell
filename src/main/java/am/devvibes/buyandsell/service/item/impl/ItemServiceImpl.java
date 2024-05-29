package am.devvibes.buyandsell.service.item.impl;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.ItemEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.repository.ItemRepository;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

	private final ItemRepository itemRepository;
	private final ItemMapper itemMapper;
	private final SecurityService securityService;

	@Override
	@Transactional
	public ItemResponseDto save(ItemRequestDto itemRequestDto, Long categoryId) {
		ItemEntity itemEntity = itemMapper.mapDtoToEntity(itemRequestDto, categoryId);
		return itemMapper.mapEntityToDto(itemRepository.save(itemEntity));
	}

	@Override
	@Transactional
	public ItemResponseDto findById(Long id) {
		ItemEntity itemEntity =
				itemRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
		return itemMapper.mapEntityToDto(itemEntity);
	}

	@Override
	@Transactional
	public List<ItemResponseDto> findAllItems() {
		return itemMapper.mapEntityListToDtoList(itemRepository.findAll());
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		String currentUserId = securityService.getCurrentUserId();
		ItemEntity itemEntity =
				itemRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));

		if (!itemEntity.getUserEntity().getId().equals(currentUserId)) {
			throw new SomethingWentWrongException(ExceptionConstants.INVALID_ACTION);
		}
		itemRepository.deleteById(id);
	}

}
