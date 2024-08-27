package am.devvibes.buyandsell.service.favoriteItems;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.entity.favoriteItems.FavoriteItemsEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.repository.favoriteItems.FavoriteItemsRepository;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.service.favoriteItems.impl.FavoriteItemsServiceImpl;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.util.CurrencyEnum;
import am.devvibes.buyandsell.util.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
				.id(1L)
				.userId("userId")
				.itemId(itemEntity.getId())
				.build();

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
		String userId = "userId1";

		doNothing().when(favoriteItemsRepository).deleteAllByUserId(userId);

		favoriteItemsService.deleteAllByUserId(userId);

		verify(favoriteItemsRepository, times(1)).deleteAllByUserId(userId);
	}

}
