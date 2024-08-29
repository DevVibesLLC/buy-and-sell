package am.devvibes.buyandsell.service.favoriteItems;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.entity.favoriteItems.FavoriteItemsEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.repository.favoriteItems.FavoriteItemsRepository;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.service.favoriteItems.impl.FavoriteItemsServiceImpl;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.util.CurrencyEnum;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FavoriteItemsServiceTest {

	@Mock
	private FavoriteItemsRepository favoriteItemsRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private FavoriteItemsServiceImpl favoriteItemsService;

	@Test
	void getUsersAllFavoriteItems() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		ItemEntity itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		FavoriteItemsEntity favoriteItems = FavoriteItemsEntity.builder()
				.userId("userId")
				.itemId(itemEntity.getId())
				.build();
		favoriteItems.setId(1L);

		when(favoriteItemsRepository.findByUserId(userEntity.getId())).thenReturn(List.of(favoriteItems));
		when(itemRepository.findAllById(List.of(itemEntity.getId()))).thenReturn(List.of(itemEntity));

		List<ItemEntity> usersAllFavoriteItems = favoriteItemsService.getUsersAllFavoriteItems(userEntity.getId());

		assertNotNull(usersAllFavoriteItems);
		assertEquals(1, usersAllFavoriteItems.size());
		assertTrue(usersAllFavoriteItems.contains(itemEntity));

		verify(favoriteItemsRepository, times(1)).findByUserId(userEntity.getId());
		verify(itemRepository, times(1)).findAllById(List.of(itemEntity.getId()));

	}

	@Test
	void getUsersAllFavoriteItems__returnsEmptyList() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		when(favoriteItemsRepository.findByUserId(userEntity.getId())).thenReturn(Collections.emptyList());

		List<ItemEntity> usersAllFavoriteItems = favoriteItemsService.getUsersAllFavoriteItems(userEntity.getId());

		assertNotNull(usersAllFavoriteItems);
		assertTrue(usersAllFavoriteItems.isEmpty());

		verify(favoriteItemsRepository, times(1)).findByUserId(userEntity.getId());
		verify(itemRepository, times(1)).findAllById(anyList());

	}

	@Test
	void getUsersIdsByItemId_itemIsFavoriteByMultipleUsers() {
		Long itemId = 1L;

		FavoriteItemsEntity favoriteItem1 = new FavoriteItemsEntity();
		favoriteItem1.setItemId(itemId);
		favoriteItem1.setUserId("userId1");

		FavoriteItemsEntity favoriteItem2 = new FavoriteItemsEntity();
		favoriteItem2.setItemId(itemId);
		favoriteItem2.setUserId("useId2");

		when(favoriteItemsRepository.findByItemId(itemId))
				.thenReturn(List.of(favoriteItem1, favoriteItem2));

		List<String> userIds = favoriteItemsService.getUsersIdsByItemId(itemId);

		assertNotNull(userIds);
		assertEquals(2, userIds.size());
		assertTrue(userIds.contains(favoriteItem1.getUserId()));
		assertTrue(userIds.contains(favoriteItem2.getUserId()));

		verify(favoriteItemsRepository, times(1)).findByItemId(itemId);
	}

	@Test
	void getUsersIdsByItemId_itemIsNotFavoriteByAnyUser() {
		Long itemId = 1L;

		when(favoriteItemsRepository.findByItemId(itemId))
				.thenReturn(Collections.emptyList());

		List<String> userIds = favoriteItemsService.getUsersIdsByItemId(itemId);

		assertNotNull(userIds);
		assertTrue(userIds.isEmpty());

		verify(favoriteItemsRepository, times(1)).findByItemId(itemId);
	}

	@Test
	void deleteAllByUserId() {
		String userId = "userId";

		doNothing().when(favoriteItemsRepository).deleteAllByUserId(userId);

		favoriteItemsService.deleteAllByUserId(userId);

		verify(favoriteItemsRepository, times(1)).deleteAllByUserId(userId);
	}

	@Test
	void addFavoriteItem_success() {
		String userId = "userId";
		Long itemId = 1L;

		when(securityService.getCurrentUserId()).thenReturn(userId);
		when(itemRepository.existsById(itemId)).thenReturn(true);
		when(favoriteItemsRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(null);

		FavoriteItemsEntity favoriteItem = FavoriteItemsEntity.builder()
				.userId(userId)
				.itemId(itemId)
				.build();
		when(favoriteItemsRepository.save(any(FavoriteItemsEntity.class))).thenReturn(favoriteItem);

		List<ItemEntity> favoriteItems = new ArrayList<>();
		when(favoriteItemsService.getUsersAllFavoriteItems(userId)).thenReturn(favoriteItems);

		List<ItemEntity> result = favoriteItemsService.addFavoriteItem(userId, itemId);

		assertNotNull(result);
		assertEquals(favoriteItems, result);

		verify(favoriteItemsRepository, times(1)).save(any(FavoriteItemsEntity.class));
	}

	@Test
	void addFavoriteItem_invalidAction() {
		String userId = "userId";
		Long itemId = 1L;

		when(securityService.getCurrentUserId()).thenReturn("differentUser");

		assertThrows(SomethingWentWrongException.class,
				() -> favoriteItemsService.addFavoriteItem(userId, itemId), ExceptionConstants.INVALID_ACTION.getString());

		verify(favoriteItemsRepository, times(0)).save(any(FavoriteItemsEntity.class));
	}

	@Test
	void addFavoriteItem_itemNotFound() {
		String userId = "userId";
		Long itemId = 1L;

		when(securityService.getCurrentUserId()).thenReturn(userId);
		when(itemRepository.existsById(itemId)).thenReturn(false);

		assertThrows(NotFoundException.class,
				() -> favoriteItemsService.addFavoriteItem(userId, itemId), ExceptionConstants.ITEM_NOT_FOUND.getString());

		verify(favoriteItemsRepository, times(0)).save(any(FavoriteItemsEntity.class));
	}

	@Test
	void addFavoriteItem_itemAlreadyInFavorites() {
		String userId = "userId";
		Long itemId = 1L;

		when(securityService.getCurrentUserId()).thenReturn(userId);
		when(itemRepository.existsById(itemId)).thenReturn(true);
		when(favoriteItemsRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(new FavoriteItemsEntity());

		assertThrows(SomethingWentWrongException.class,
				() -> favoriteItemsService.addFavoriteItem(userId, itemId), ExceptionConstants.ITEM_ALREADY_EXISTS_IN_FAVORITES.getString());

		verify(favoriteItemsRepository, times(0)).save(any(FavoriteItemsEntity.class));
	}

	@Test
	void removeFavoriteItem_success() {
		String userId = "userId";
		Long itemId = 1L;

		FavoriteItemsEntity favoriteItem = FavoriteItemsEntity.builder().userId(userId).itemId(itemId).build();

		when(securityService.getCurrentUserId()).thenReturn(userId);
		when(favoriteItemsRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(favoriteItem);

		List<Long> remainingItemIds = List.of(2L, 3L);

		when(favoriteItemsRepository.findByUserId(userId))
				.thenReturn(remainingItemIds.stream().map(id -> {
					FavoriteItemsEntity entity = new FavoriteItemsEntity();
					entity.setItemId(id);
					return entity;
				}).collect(Collectors.toList()));
		when(itemRepository.findAllById(remainingItemIds)).thenReturn(new ArrayList<>());

		List<ItemEntity> result = favoriteItemsService.removeFavoriteItem(userId, itemId);

		assertNotNull(result);
		assertEquals(0, result.size());

		verify(favoriteItemsRepository, times(1)).delete(favoriteItem);
		verify(favoriteItemsRepository, times(1)).findByUserId(userId);
		verify(itemRepository, times(1)).findAllById(remainingItemIds);
	}

	@Test
	void removeFavoriteItem_invalidUserId() {
		String userId = "userId";
		Long itemId = 1L;

		when(securityService.getCurrentUserId()).thenReturn("userId2");

		assertThrows(SomethingWentWrongException.class,
				() -> favoriteItemsService.removeFavoriteItem(userId, itemId), ExceptionConstants.INVALID_ACTION.getString());

		verify(favoriteItemsRepository, times(0)).delete(any(FavoriteItemsEntity.class));
	}

}
