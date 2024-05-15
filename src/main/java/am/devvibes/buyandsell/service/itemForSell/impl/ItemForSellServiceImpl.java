package am.devvibes.buyandsell.service.itemForSell.impl;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.entity.ItemForSell;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.ItemForSellMapper;
import am.devvibes.buyandsell.repository.ItemForSellRepository;
import am.devvibes.buyandsell.service.itemForSell.ItemForSellService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemForSellServiceImpl implements ItemForSellService {

	private final ItemForSellRepository itemForSellRepository;
	private final ItemForSellMapper itemForSellMapper;

	@Override
	@Transactional
	public ItemForSellResponseDto saveItemForSell(ItemForSellRequestDto itemForSellRequestDto) {
		ItemForSell itemForSellEntity = itemForSellMapper.mapDtoToEntity(itemForSellRequestDto);
		ItemForSell savedItemForSell = itemForSellRepository.save(itemForSellEntity);
		return itemForSellMapper.mapEntityToDto(savedItemForSell);
	}

	@Override
	@Transactional
	public ItemForSellResponseDto findItemForSellById(Long id) {
		ItemForSell itemForSellEntity = itemForSellRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
		return itemForSellMapper.mapEntityToDto(itemForSellEntity);
	}

	@Override
	@Transactional
	public List<ItemForSellResponseDto> findAllItemForSells() {
		List<ItemForSell> itemForSellEntityList = itemForSellRepository.findAll();
		return itemForSellMapper.mapEntityListToDtoList(itemForSellEntityList);
	}

	@Override
	@Transactional
	public void deleteItemForSell(Long id) {
		itemForSellRepository.deleteById(id);
	}

}
