package am.devvibes.buyandsell.repository.cart;

import am.devvibes.buyandsell.entity.cart.CartEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<CartEntity, Long> {

	List<CartEntity> findByUserId(String userId);

	void deleteByUserAndItem(UserEntity userId, ItemEntity itemId);
}