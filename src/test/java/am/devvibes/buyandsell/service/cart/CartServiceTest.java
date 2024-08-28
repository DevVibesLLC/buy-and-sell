package am.devvibes.buyandsell.service.cart;

import am.devvibes.buyandsell.entity.cart.CartEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.repository.cart.CartRepository;
import am.devvibes.buyandsell.service.cart.impl.CartServiceImpl;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

	@Mock
	private CartRepository cartRepository;

	@Mock
	private ItemService itemService;

	@Mock
	private UserService userService;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private CartServiceImpl cartService;

	@Test
	void addItemToCart__success() {
		Long itemId = 1L;
		String userId = "userId";

		UserEntity userEntity = new UserEntity();
		userEntity.setId(userId);

		ItemEntity itemEntity = ItemEntity.builder()
				.userEntity(userEntity)
				.build();
		itemEntity.setId(itemId);

		CartEntity cartEntity = CartEntity.builder()
				.user(userEntity)
				.item(itemEntity)
				.build();
		cartEntity.setId(1L);

		when(securityService.getCurrentUserId()).thenReturn(userId);
		when(userService.findUserById(userId)).thenReturn(userEntity);
		when(itemService.findEntityById(itemId)).thenReturn(itemEntity);
		when(cartRepository.save(any(CartEntity.class))).thenReturn(cartEntity);
		when(cartRepository.findByUserId(userId)).thenReturn(List.of(cartEntity));

		List<ItemEntity> cartItems = cartService.addItemToCart(itemId);

		assertNotNull(cartItems);
		assertFalse(cartItems.isEmpty());
		assertEquals(1, cartItems.size());

		verify(cartRepository, times(1)).save(any(CartEntity.class));
	}

	@Test
	void removeItemFromCart__success() {

		UserEntity currentUser = new UserEntity();
		currentUser.setId("currentUserId");

		ItemEntity itemEntity = new ItemEntity();
		itemEntity.setId(1L);

		CartEntity cartEntity = new CartEntity();
		cartEntity.setUser(currentUser);
		cartEntity.setItem(itemEntity);

		when(securityService.getCurrentUserId()).thenReturn("currentUserId");
		when(userService.findUserById("currentUserId")).thenReturn(currentUser);
		when(itemService.findEntityById(1L)).thenReturn(itemEntity);
		doNothing().when(cartRepository).deleteByUserAndItem(currentUser, itemEntity);
		when(cartRepository.findByUserId(currentUser.getId())).thenReturn(new ArrayList<>());

		List<ItemEntity> remainingItems = cartService.removeItemFromCart(1L);

		assertNotNull(remainingItems);
		assertTrue(remainingItems.isEmpty());

		verify(securityService, times(2)).getCurrentUserId();
		verify(userService, times(1)).findUserById("currentUserId");
		verify(itemService, times(1)).findEntityById(1L);
		verify(cartRepository, times(1)).deleteByUserAndItem(currentUser, itemEntity);
		verify(cartRepository, times(1)).findByUserId(currentUser.getId());
	}

	@Test
	void getUsersCart__success() {

		String currentUserId = "currentUserId";

		UserEntity currentUser = new UserEntity();
		currentUser.setId(currentUserId);

		ItemEntity itemEntity1 = new ItemEntity();
		itemEntity1.setId(1L);
		itemEntity1.setTitle("Item 1");

		ItemEntity itemEntity2 = new ItemEntity();
		itemEntity2.setId(2L);
		itemEntity2.setTitle("Item 2");

		CartEntity cartEntity1 = new CartEntity();
		cartEntity1.setUser(currentUser);
		cartEntity1.setItem(itemEntity1);

		CartEntity cartEntity2 = new CartEntity();
		cartEntity2.setUser(currentUser);
		cartEntity2.setItem(itemEntity2);

		List<CartEntity> cartEntities = List.of(cartEntity1, cartEntity2);

		when(securityService.getCurrentUserId()).thenReturn(currentUserId);
		when(cartRepository.findByUserId(currentUserId)).thenReturn(cartEntities);

		List<ItemEntity> cartItems = cartService.getUsersCart();

		assertNotNull(cartItems);
		assertEquals(2, cartItems.size());
		assertTrue(cartItems.contains(itemEntity1));
		assertTrue(cartItems.contains(itemEntity2));

		verify(securityService, times(1)).getCurrentUserId();
		verify(cartRepository, times(1)).findByUserId(currentUserId);
	}

}
