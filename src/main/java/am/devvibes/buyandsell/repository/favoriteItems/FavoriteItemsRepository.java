package am.devvibes.buyandsell.repository.favoriteItems;

import am.devvibes.buyandsell.entity.favoriteItems.FavoriteItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteItemsRepository extends JpaRepository<FavoriteItemsEntity, Long> {

	List<FavoriteItemsEntity> findByUserId(String userId);

	void deleteAllByItemId(Long itemId);

	void deleteAllByUserId(String userId);

	FavoriteItemsEntity findByUserIdAndItemId(String userId, Long itemId);
}