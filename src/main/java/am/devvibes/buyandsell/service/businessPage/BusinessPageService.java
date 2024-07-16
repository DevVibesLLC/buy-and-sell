package am.devvibes.buyandsell.service.businessPage;

import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageResponseDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;

public interface BusinessPageService {

	BusinessPageEntity registerBusinessPage(BusinessPageRequestDto businessPage, Long categoryId);

	BusinessPageEntity findBusinessPageById(Long id);

	BusinessPageEntity addItemFromBusinessPage(ItemRequestDto itemRequestDto, Long businessPageId);

	BusinessPageEntity updateItemFromBusinessPage(ItemRequestDto itemRequestDto, Long businessPageId, Long itemId);

	void deleteItemFromBusinessPage(Long businessPageId, Long itemId);
}
