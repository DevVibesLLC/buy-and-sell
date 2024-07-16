package am.devvibes.buyandsell.service.businessPage.impl;

import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.businessPage.BusinessPageMapper;
import am.devvibes.buyandsell.repository.businessPage.BusinessPageRepository;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.service.businessPage.BusinessPageService;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessPageServiceImpl implements BusinessPageService {

	private final BusinessPageRepository businessPageRepository;
	private final BusinessPageMapper businessPageMapper;
	private final ItemService itemService;
	private final SecurityService securityService;
	private final ItemRepository itemRepository;

	@Override
	public BusinessPageEntity registerBusinessPage(BusinessPageRequestDto businessPage, Long categoryId) {
		return businessPageRepository.save(businessPageMapper.mapDtoToEntity(businessPage, categoryId));
	}

	@Override
	public BusinessPageEntity findBusinessPageById(Long id) {
		return businessPageRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.BUSINESS_PAGE_NOT_FOUND));
	}

	@Override
	@Transactional
	public BusinessPageEntity addItemFromBusinessPage(ItemRequestDto itemRequestDto, Long businessPageId) {
		BusinessPageEntity businessPageEntity = findBusinessPageById(businessPageId);
		ItemEntity itemEntity = itemService.saveFromBusiness(itemRequestDto, businessPageEntity);
		businessPageEntity.getAdds().add(itemEntity);
		return businessPageRepository.save(businessPageEntity);
	}

	@Override
	@Transactional
	public BusinessPageEntity updateItemFromBusinessPage(ItemRequestDto itemRequestDto, Long businessPageId, Long itemId) {
		BusinessPageEntity businessPageEntity = findBusinessPageById(businessPageId);
		ItemEntity itemEntity = itemService.updateFromBusiness(itemRequestDto, businessPageEntity, itemId);
		businessPageEntity.getAdds().add(itemEntity);
		return businessPageRepository.save(businessPageEntity);
	}

	@Override
	@Transactional
	public void deleteItemFromBusinessPage(Long businessPageId, Long itemId) {
		BusinessPageEntity businessPageEntity = findBusinessPageById(businessPageId);
		if (securityService.getCurrentUserId().equals(businessPageEntity.getOwner().getId())) {
			ItemEntity itemEntity = businessPageEntity.getAdds()
					.stream()
					.filter(i -> i.getId().equals(itemId))
					.findFirst()
					.orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));

			itemEntity.setStatus(Status.DELETED);
			itemRepository.save(itemEntity);
		}
	}

}
