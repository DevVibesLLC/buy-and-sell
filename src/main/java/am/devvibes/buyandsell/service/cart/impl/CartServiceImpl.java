package am.devvibes.buyandsell.service.cart.impl;

import am.devvibes.buyandsell.entity.cart.CartEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.repository.cart.CartRepository;
import am.devvibes.buyandsell.service.cart.CartService;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ItemService itemService;
	private final UserService userService;
	private final SecurityService securityService;

	@Override
	@Transactional
	public List<ItemEntity> addItemToCart(Long itemId) {
		CartEntity cartEntity = CartEntity.builder()
				.user(userService.findUserById(securityService.getCurrentUserId()))
				.item(itemService.findEntityById(itemId))
				.build();
		cartRepository.save(cartEntity);
		return getUsersCart();
	}

	@Override
	@Transactional
	public List<ItemEntity> removeItemFromCart(Long itemId) {
		UserEntity currentUser = userService.findUserById(securityService.getCurrentUserId());
		ItemEntity itemEntity = itemService.findEntityById(itemId);
		cartRepository.deleteByUserAndItem(currentUser, itemEntity);
		return getUsersCart();
	}

	@Override
	@Transactional
	public List<ItemEntity> getUsersCart() {
		return cartRepository.findByUserId(securityService.getCurrentUserId())
				.stream()
				.map(CartEntity::getItem)
				.toList();
	}

}
