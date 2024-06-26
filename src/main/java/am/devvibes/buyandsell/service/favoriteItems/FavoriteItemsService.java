package am.devvibes.buyandsell.service.favoriteItems;

import am.devvibes.buyandsell.entity.favoriteItems.FavoriteItemsEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;

import java.util.List;

public interface FavoriteItemsService {

	List<ItemEntity> getUsersAllFavoriteItems(String userId);

	void deleteAllByUserId(String userId);

	List<ItemEntity> addFavoriteItem(String userId, Long itemId);

	List<ItemEntity> removeFavoriteItem(String userId, Long itemId);

}
