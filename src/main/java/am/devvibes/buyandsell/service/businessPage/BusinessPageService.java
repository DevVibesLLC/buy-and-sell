package am.devvibes.buyandsell.service.businessPage;

import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageUpdateDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemUpdateDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;

public interface BusinessPageService {

	BusinessPageEntity registerBusinessPage(BusinessPageRequestDto businessPage, Long categoryId);

	BusinessPageEntity findBusinessPageById(Long id);

	BusinessPageEntity addItemFromBusinessPage(ItemRequestDto itemRequestDto, Long businessPageId);

	BusinessPageEntity updateItemFromBusinessPage(ItemUpdateDto itemUpdateDto, Long businessPageId, Long itemId);

	BusinessPageEntity updateBusinessPage(BusinessPageUpdateDto businessPageUpdateDto, Long businessPageId);

	ItemEntity deleteItemFromBusinessPage(Long businessPageId, Long itemId);

}
