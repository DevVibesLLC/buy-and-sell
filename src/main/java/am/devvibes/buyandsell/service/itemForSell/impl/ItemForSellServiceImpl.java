package am.devvibes.buyandsell.service.itemForSell.impl;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.entity.ItemForSellEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.ItemForSellMapper;
import am.devvibes.buyandsell.repository.ItemForSellRepository;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.itemForSell.ItemForSellService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
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
	private final SecurityService securityService;
	private final UserService userService;

	@Override
	@Transactional
	public ItemForSellResponseDto saveItemForSell(ItemForSellRequestDto itemForSellRequestDto) {
		ItemForSellEntity itemForSellEntity = itemForSellMapper.mapDtoToEntity(itemForSellRequestDto);
		itemForSellEntity.setUserEntity(userService.findUserById(securityService.getCurrentUserId()));
		ItemForSellEntity savedItemForSell = itemForSellRepository.save(itemForSellEntity);
		return itemForSellMapper.mapEntityToDto(savedItemForSell);
	}

	@Override
	@Transactional
	public ItemForSellResponseDto findItemForSellById(Long id) {
		ItemForSellEntity itemForSellEntity = itemForSellRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
		return itemForSellMapper.mapEntityToDto(itemForSellEntity);
	}

	@Override
	@Transactional
	public List<ItemForSellResponseDto> findAllItemForSells() {
		List<ItemForSellEntity> itemForSellEntityList = itemForSellRepository.findAll();
		return itemForSellMapper.mapEntityListToDtoList(itemForSellEntityList);
	}

	@Override
	@Transactional
	public void deleteItemForSell(Long id) {
		ItemForSellEntity itemForSellEntity = itemForSellRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
		if (!itemForSellEntity.getUserEntity().getId().equals(securityService.getCurrentUserId())) {
			throw new SomethingWentWrongException(ExceptionConstants.INVALID_ACTION);
		}
		itemForSellRepository.deleteById(id);
	}

	//	private ItemForSellEntity setUser(ItemForSellEntity itemForSellEntity) {
	//		itemForSellEntity.setUser();
	//	}

}
