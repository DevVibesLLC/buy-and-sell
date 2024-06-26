package am.devvibes.buyandsell.service.favoriteItems.impl;

import am.devvibes.buyandsell.entity.favoriteItems.FavoriteItemsEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.repository.favoriteItems.FavoriteItemsRepository;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.service.favoriteItems.FavoriteItemsService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteItemsServiceImpl implements FavoriteItemsService {

	private final FavoriteItemsRepository favoriteItemsRepository;
	private final ItemRepository itemRepository;
	private final SecurityService securityService;

	@Override
	public List<ItemEntity> getUsersAllFavoriteItems(String userId) {
		List<Long> idsList =
				favoriteItemsRepository.findByUserId(userId).stream().map(FavoriteItemsEntity::getItemId).toList();

		return itemRepository.findAllById(idsList);
	}

	@Override
	public void deleteAllByUserId(String userId) {
		favoriteItemsRepository.deleteAllByUserId(userId);
	}

	@Override
	public List<ItemEntity> addFavoriteItem(String userId, Long itemId) {
		if (!userId.equals(securityService.getCurrentUserId())) {
			throw new SomethingWentWrongException(ExceptionConstants.INVALID_ACTION);
		}

		if (!itemRepository.existsById(itemId)) {
			throw new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND);
		}

		if (favoriteItemsRepository.findByUserIdAndItemId(userId, itemId) != null)
			throw new SomethingWentWrongException(ExceptionConstants.ITEM_ALREADY_EXISTS_IN_FAVORITES);

		FavoriteItemsEntity favoriteItem = FavoriteItemsEntity.builder().userId(userId).itemId(itemId).build();
		favoriteItemsRepository.save(favoriteItem);

		return getUsersAllFavoriteItems(userId);
	}

	@Override
	public List<ItemEntity> removeFavoriteItem(String userId, Long itemId) {
		if (!userId.equals(securityService.getCurrentUserId())) {
			throw new SomethingWentWrongException(ExceptionConstants.INVALID_ACTION);
		}

		favoriteItemsRepository.delete(favoriteItemsRepository.findByUserIdAndItemId(userId, itemId));

		List<Long> idsList =
				favoriteItemsRepository.findByUserId(userId).stream().map(FavoriteItemsEntity::getItemId).toList();

		return itemRepository.findAllById(idsList);
	}

}
