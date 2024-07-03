package am.devvibes.buyandsell.service.cart;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.item.ItemEntity;

import java.util.List;

public interface CartService {

	List<ItemEntity> addItemToCart(Long itemId);

	List<ItemEntity> removeItemFromCart(Long itemId);

	List<ItemEntity> getUsersCart();

}
