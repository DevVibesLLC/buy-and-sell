package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.item.ItemUpdateDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.priceHistory.PriceHistoryEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.repository.priceHistory.PriceHistoryRepository;
import am.devvibes.buyandsell.repository.user.UserRepository;
import am.devvibes.buyandsell.service.favoriteItems.FavoriteItemsService;
import am.devvibes.buyandsell.service.item.impl.ItemServiceImpl;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.util.CurrencyEnum;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.Status;
import am.devvibes.buyandsell.util.page.CustomPageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private ItemMapper itemMapper;

	@Mock
	private PriceHistoryRepository priceHistoryRepository;

	@Mock
	private FavoriteItemsService favoriteItemsService;

	@Mock
	private ValueService valueService;

	@Mock
	private SecurityService securityService;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ItemServiceImpl itemService;

	private Long categoryId;
	private ItemRequestDto itemRequestDto;
	private ItemEntity itemEntity;

	@Test
	void saveItem() {

		categoryId = 1L;
		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		when(itemMapper.mapDtoToEntity(itemRequestDto, categoryId)).thenReturn(itemEntity);
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);

		ItemEntity result = itemService.save(itemRequestDto, categoryId);

		assertNotNull(result);

		PriceHistoryEntity priceHistoryEntity = result.getPriceHistories().getFirst();

		assertNotNull(priceHistoryEntity);
		assertNotNull(priceHistoryEntity.getPrice());
		assertEquals(itemRequestDto.getDescription(), result.getDescription());
		assertNull(result.getBusinessPage());

		verify(itemMapper, times(1)).mapDtoToEntity(itemRequestDto, categoryId);
		verify(itemRepository, times(1)).save(itemEntity);
		verify(priceHistoryRepository, times(1)).save(any(PriceHistoryEntity.class));
	}

	@Test
	void saveItemFromBusinessPage() {

		BusinessPageEntity businessPage = BusinessPageEntity.builder()
				.title("Business Page title")
				.description("Business Page description for unit test")
				.phoneNumbers(List.of("+37499999999"))
				.email("businessPage@gmail.com")
				.build();

		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.businessPage(businessPage)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		when(itemMapper.mapDtoToEntityFromBusiness(itemRequestDto, businessPage)).thenReturn(itemEntity);
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);

		ItemEntity result = itemService.saveFromBusiness(itemRequestDto, businessPage);

		assertNotNull(result);

		PriceHistoryEntity priceHistoryEntity = result.getPriceHistories().getFirst();

		assertNotNull(priceHistoryEntity);
		assertNotNull(priceHistoryEntity.getPrice());
		assertNotNull(result.getBusinessPage());
		assertEquals(itemRequestDto.getDescription(), result.getDescription());

		verify(itemMapper, times(1)).mapDtoToEntityFromBusiness(itemRequestDto, businessPage);
		verify(itemRepository, times(1)).save(itemEntity);
		verify(priceHistoryRepository, times(1)).save(any(PriceHistoryEntity.class));
	}

	@Test
	void findItemById() {
		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.viewedUsersId(List.of("UserId1", "UserId2"))
				.countOfViews(15L)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);
		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));
		when(securityService.getCurrentUserId()).thenReturn("UserId1");

		itemRepository.save(itemEntity);
		ItemEntity entity = itemService.findById(itemEntity.getId());

		assertNotNull(entity);
		assertEquals(itemEntity.getId(),entity.getId());

		verify(itemRepository, times(1)).findById(entity.getId());

	}

	@Test
	void findItemByIdWithoutViewedUserId() {
		List<String> list = new ArrayList<>();
		list.add("UserId1");
		list.add("UserId2");

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.viewedUsersId(list)
				.countOfViews(15L)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);
		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));
		when(securityService.getCurrentUserId()).thenReturn("UserId3");

		itemRepository.save(itemEntity);
		ItemEntity entity = itemService.findById(itemEntity.getId());

		assertNotNull(entity);
		assertEquals(itemEntity.getId(),entity.getId());

		verify(itemRepository, times(1)).findById(entity.getId());

	}

	@Test
	void findEntityById() {

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.viewedUsersId(List.of("UserId1", "UserId2"))
				.countOfViews(15L)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);
		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));

		itemRepository.save(itemEntity);
		ItemEntity entity = itemService.findEntityById(itemEntity.getId());

		assertNotNull(entity);
		assertEquals(entity.getId(),entity.getId());

		verify(itemRepository, times(1)).findById(entity.getId());
	}


	@Test
	void findItemByIdWithDeletedStatus() {
		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.DELETED)
				.viewedUsersId(List.of("UserId1", "UserId2"))
				.countOfViews(15L)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));

		assertThrows(NotFoundException.class, () -> itemService.findById(itemEntity.getId()), ExceptionConstants.ITEM_NOT_FOUND.getString());

		verify(itemRepository, times(1)).findById(itemEntity.getId());

	}

	@Test
	void updateItem() {

		categoryId = 1L;
		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
				.title("New Item updated title")
				.build();

		when(itemMapper.mapDtoToEntity(itemRequestDto, categoryId)).thenReturn(itemEntity);
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);

		ItemEntity result = itemService.save(itemRequestDto, categoryId);

		when(itemRepository.findById(result.getId())).thenReturn(Optional.of(result));

		ItemEntity updatedEntity = itemService.update(itemUpdateDto, categoryId, result.getId());

		assertNotNull(updatedEntity);
		assertNotEquals(itemRequestDto.getTitle(), updatedEntity.getTitle());
		assertEquals(result.getDescription(), updatedEntity.getDescription());

		verify(itemMapper, times(1)).mapDtoToEntity(itemRequestDto, categoryId);
		verify(itemRepository, times(2)).save(itemEntity);
	}

	@Test
	void updateItemWithDeletedStatus() {

		categoryId = 1L;
		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.DELETED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
				.title("New Item updated title")
				.build();


		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));

		assertThrows(SomethingWentWrongException.class, () ->
				itemService.update(itemUpdateDto, categoryId, itemEntity.getId()), ExceptionConstants.INVALID_ACTION.getString());

		verify(itemRepository, times(0)).save(any(ItemEntity.class));

	}

	@Test
	void updateItemWithPriceChange() {

		Long categoryId = 1L;

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500)) // Original price
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L); // Set the ID for the itemEntity

		ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
				.title("New Item updated title")
				.price(new BigDecimal(2000)) // New price, different from the old price
				.build();

		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));
		when(itemRepository.save(any(ItemEntity.class))).thenReturn(itemEntity);

		ItemEntity updatedEntity = itemService.update(itemUpdateDto, categoryId, itemEntity.getId());

		assertNotNull(updatedEntity);
		assertEquals(itemUpdateDto.getPrice(), updatedEntity.getPrice().getPrice());
		verify(favoriteItemsService, times(1)).getUsersIdsByItemId(updatedEntity.getId());
		verify(itemRepository, times(2)).save(any(ItemEntity.class));
	}

	@Test
	void updateItemFromBusinessPage() {

		BusinessPageEntity businessPage = BusinessPageEntity.builder()
				.title("Business Page title")
				.description("Business Page description for unit test")
				.phoneNumbers(List.of("+37499999999"))
				.email("businessPage@gmail.com")
				.build();
		businessPage.setId(1L);

		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.businessPage(businessPage)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
				.title("New Item updated title")
				.build();

		when(itemMapper.mapDtoToEntityFromBusiness(itemRequestDto, businessPage)).thenReturn(itemEntity);
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);

		ItemEntity result = itemService.saveFromBusiness(itemRequestDto, businessPage);

		when(itemRepository.findById(result.getId())).thenReturn(Optional.of(itemEntity));

		ItemEntity updatedEntity = itemService.updateFromBusiness(itemUpdateDto, businessPage, result.getId());

		assertNotNull(updatedEntity);
		assertNotEquals(itemRequestDto.getTitle(), updatedEntity.getTitle());
		assertEquals(result.getDescription(), updatedEntity.getDescription());

		verify(itemMapper, times(1)).mapDtoToEntityFromBusiness(itemRequestDto, businessPage);
		verify(itemRepository, times(2)).save(itemEntity);

	}

	@Test
	void updateItemFromBusinessPageWithDeletedStatus() {

		BusinessPageEntity businessPage = BusinessPageEntity.builder()
				.title("Business Page title")
				.description("Business Page description for unit test")
				.phoneNumbers(List.of("+37499999999"))
				.email("businessPage@gmail.com")
				.build();
		businessPage.setId(1L);

		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.DELETED)
				.businessPage(businessPage)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
				.title("New Item updated title")
				.build();


		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));

		assertThrows(SomethingWentWrongException.class, () ->
			itemService.updateFromBusiness(itemUpdateDto, businessPage, itemEntity.getId()), ExceptionConstants.INVALID_ACTION.getString());

		verify(itemRepository, times(0)).save(any(ItemEntity.class));

	}

	@Test
	void updateItemFromBusinessPageWithPriceChange() {

		BusinessPageEntity businessPage = BusinessPageEntity.builder()
				.title("Business Page title")
				.description("Business Page description for unit test")
				.phoneNumbers(List.of("+37499999999"))
				.email("businessPage@gmail.com")
				.build();
		businessPage.setId(1L);

		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.businessPage(businessPage)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
				.title("New Item updated title")
				.price(new BigDecimal(2000))
				.build();

		UserEntity user1 = new UserEntity();
		user1.setId("user1");
		user1.setEmail("user1@example.com");

		UserEntity user2 = new UserEntity();
		user2.setId("user2");
		user2.setEmail("user2@example.com");

		List<String> userIds = List.of("user1", "user2");
		List<UserEntity> userEntities = List.of(user1, user2);

		when(itemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));
		when(favoriteItemsService.getUsersIdsByItemId(itemEntity.getId())).thenReturn(userIds);
		when(userRepository.findAllById(userIds)).thenReturn(userEntities);
		when(itemRepository.save(any(ItemEntity.class))).thenReturn(itemEntity);

		ItemEntity updatedEntity = itemService.updateFromBusiness(itemUpdateDto, businessPage, itemEntity.getId());

		assertNotNull(updatedEntity);
		assertEquals(itemUpdateDto.getPrice(), updatedEntity.getPrice().getPrice());

		verify(favoriteItemsService, times(1)).getUsersIdsByItemId(updatedEntity.getId());
		verify(userRepository, times(1)).findAllById(userIds);
		verify(itemRepository, times(2)).save(any(ItemEntity.class));

	}



	@Test
	void getAllItems() {

		categoryId = 1L;
		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		List<ItemEntity> list = List.of(itemEntity);
		PageRequest pageRequest = CustomPageRequest.from(1, 10, Sort.unsorted());
		Page<ItemEntity> page = new PageImpl<>(list,pageRequest,list.size());

		when(itemMapper.mapDtoToEntity(itemRequestDto, categoryId)).thenReturn(itemEntity);
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);
		when(itemRepository.findAll(pageRequest)).thenReturn(page);

		itemService.save(itemRequestDto, categoryId);

		Page<ItemResponseDto> allItems = itemService.findAllItems(pageRequest);

		assertNotNull(allItems);
		assertEquals(1, allItems.getTotalElements());

		verify(itemMapper, times(1)).mapDtoToEntity(itemRequestDto, categoryId);
		verify(itemRepository, times(1)).save(itemEntity);
	}

	@Test
	void searchAndThereIsResult() {

		categoryId = 1L;
		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		SearchDto searchDto = new SearchDto("New");

		List<ItemEntity> list = List.of(itemEntity);

		when(itemMapper.mapDtoToEntity(itemRequestDto, categoryId)).thenReturn(itemEntity);
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);
		when(itemRepository.findAll(any(Specification.class))).thenReturn(list);

		itemService.save(itemRequestDto, categoryId);
		List<ItemEntity> itemEntities = itemService.searchItems(searchDto);

		assertNotNull(itemEntities);
		assertEquals(1, itemEntities.size());

		verify(itemMapper, times(1)).mapDtoToEntity(itemRequestDto, categoryId);
		verify(itemRepository, times(1)).save(itemEntity);
		verify(itemRepository, times(1)).findAll(any(Specification.class));
	}

	@Test
	void searchAndThereIsNoResult() {

		categoryId = 1L;
		itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		SearchDto searchDto = new SearchDto("Bmw");

		when(itemMapper.mapDtoToEntity(itemRequestDto, categoryId)).thenReturn(itemEntity);
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);
		when(itemRepository.findAll(any(Specification.class))).thenReturn(new ArrayList());

		itemService.save(itemRequestDto, categoryId);
		List<ItemEntity> itemEntities = itemService.searchItems(searchDto);

		assertNotNull(itemEntities);
		assertEquals(0, itemEntities.size());
		assertNotEquals(1, itemEntities.size());

		verify(itemMapper, times(1)).mapDtoToEntity(itemRequestDto, categoryId);
		verify(itemRepository, times(1)).save(itemEntity);
		verify(itemRepository, times(1)).findAll(any(Specification.class));
	}

	@Test
	void findUsersItems() {

		itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");
		userEntity.setEmail("user@gmail.com");

		when(securityService.getCurrentUserId()).thenReturn("userId");
		when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
		when(itemRepository.findByUserEntity(userEntity)).thenReturn(List.of(itemEntity));

		List<ItemEntity> usersItems = itemService.findUsersItems();

		assertNotNull(usersItems);
		assertEquals(1,usersItems.size());

		verify(securityService, times(1)).getCurrentUserId();
		verify(userRepository, times(1)).findById(userEntity.getId());
		verify(itemRepository, times(1)).findByUserEntity(userEntity);
	}

}