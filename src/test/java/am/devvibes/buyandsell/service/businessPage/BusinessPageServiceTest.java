package am.devvibes.buyandsell.service.businessPage;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageUpdateDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemUpdateDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.businessPage.BusinessPageMapper;
import am.devvibes.buyandsell.repository.businessPage.BusinessPageRepository;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.service.businessPage.impl.BusinessPageServiceImpl;
import am.devvibes.buyandsell.service.item.ItemService;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BusinessPageServiceTest {

	@Mock
	private BusinessPageRepository businessPageRepository;

	@Mock
	private BusinessPageMapper businessPageMapper;

	@Mock
	private ItemService itemService;

	@Mock
	private SecurityService securityService;

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private BusinessPageServiceImpl businessPageService;

	@Test
	void registerBusinessPage__success() {

		Long categoryId = 1L;
		BusinessPageRequestDto businessPageRequestDto = BusinessPageRequestDto.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.cityId(15L)
				.build();

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.build();

		when(businessPageMapper.mapDtoToEntity(businessPageRequestDto, categoryId)).thenReturn(businessPageEntity);
		when(businessPageRepository.save(businessPageEntity)).thenReturn(businessPageEntity);

		BusinessPageEntity businessPage =
				businessPageService.registerBusinessPage(businessPageRequestDto, categoryId);

		assertNotNull(businessPage);
		assertEquals(businessPageRequestDto.getAddress(),businessPage.getAddress());

		verify(businessPageMapper, times(1)).mapDtoToEntity(businessPageRequestDto, categoryId);
		verify(businessPageRepository, times(1)).save(businessPageEntity);
	}

	@Test
	void findBusinessPageById__success() {

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.build();
		businessPageEntity.setId(1L);

		when(businessPageRepository.save(businessPageEntity)).thenReturn(businessPageEntity);
		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));

		businessPageRepository.save(businessPageEntity);
		BusinessPageEntity businessPage =
				businessPageService.findBusinessPageById(businessPageEntity.getId());

		assertNotNull(businessPage);
		assertEquals(businessPageEntity.getId(),businessPage.getId());

		verify(businessPageRepository, times(1)).findById(businessPage.getId());
	}

	@Test
	void findBusinessPageById__wrongId() {

		Long businessPageId = 1L;
		when(businessPageRepository.findById(businessPageId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> businessPageService.findBusinessPageById(1L), ExceptionConstants.BUSINESS_PAGE_NOT_FOUND.getString());

		verify(businessPageRepository, times(1)).findById(businessPageId);
	}

	@Test
	void addItemFromBusinessPage__success() {
		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.adds(new ArrayList<>())
				.build();
		businessPageEntity.setId(1L);

		ItemRequestDto itemRequestDto = ItemRequestDto.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.price(new BigDecimal(1500))
				.currency(CurrencyEnum.USD)
				.cityId(15L)
				.phoneNumbers(List.of("+37499999999"))
				.build();

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

		when(businessPageRepository.save(businessPageEntity)).thenReturn(businessPageEntity);
		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));
		when(itemService.saveFromBusiness(itemRequestDto, businessPageEntity)).thenReturn(itemEntity);

		BusinessPageEntity businessPage =
				businessPageService.addItemFromBusinessPage(itemRequestDto, businessPageEntity.getId());

		assertNotNull(businessPage);
		assertEquals(businessPageEntity.getId(),businessPage.getId());

		verify(businessPageRepository, times(1)).findById(businessPage.getId());
		verify(businessPageRepository, times(1)).save(businessPageEntity);
	}

	@Test
	void updateBusinessPage__success() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.adds(new ArrayList<>())
				.owner(userEntity)
				.build();
		businessPageEntity.setId(1L);

		BusinessPageUpdateDto businessPageUpdateDto =
				BusinessPageUpdateDto.builder().title("new updated business page title").build();

		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));
		when(businessPageRepository.save(businessPageEntity)).thenReturn(businessPageEntity);
		when(securityService.getCurrentUserId()).thenReturn("userId");

		BusinessPageEntity updatedBusinessPage =
				businessPageService.updateBusinessPage(businessPageUpdateDto, businessPageEntity.getId());

		assertNotNull(updatedBusinessPage);
		assertEquals(businessPageEntity.getDescription(), updatedBusinessPage.getDescription());
		assertEquals(businessPageUpdateDto.getTitle(), updatedBusinessPage.getTitle());
		assertNotEquals("business page title", updatedBusinessPage.getTitle());

		verify(businessPageRepository,times(1)).findById(businessPageEntity.getId());
		verify(businessPageRepository,times(1)).save(businessPageEntity);

	}

	@Test
	void updateBusinessPage__wrongUserId() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.adds(new ArrayList<>())
				.owner(userEntity)
				.build();
		businessPageEntity.setId(1L);

		BusinessPageUpdateDto businessPageUpdateDto =
				BusinessPageUpdateDto.builder().title("new updated business page title").build();

		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));
		when(securityService.getCurrentUserId()).thenReturn("userId2");

		assertThrows(SomethingWentWrongException.class,
				() -> businessPageService.updateBusinessPage(businessPageUpdateDto, businessPageEntity.getId()), ExceptionConstants.INVALID_ACTION.getString());

		verify(businessPageRepository,times(1)).findById(businessPageEntity.getId());
		verify(businessPageRepository,times(0)).save(businessPageEntity);

	}

	@Test
	void updateItemFromBusinessPage__success(){
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.adds(new ArrayList<>())
				.owner(userEntity)
				.build();
		businessPageEntity.setId(1L);

		ItemEntity itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.businessPage(businessPageEntity)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();

		ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
				.title("New Item updated title")
				.build();

		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));
		when(businessPageRepository.save(businessPageEntity)).thenReturn(businessPageEntity);
		when(itemService.updateFromBusiness(itemUpdateDto, businessPageEntity, itemEntity.getId())).thenReturn(itemEntity);

		BusinessPageEntity businessPage =
				businessPageService.updateItemFromBusinessPage(itemUpdateDto, businessPageEntity.getId(),
						itemEntity.getId());

		assertNotNull(businessPage);
		assertNotEquals(itemEntity.getTitle(), itemUpdateDto.getTitle());
		assertTrue(businessPage.getAdds().contains(itemEntity));

		verify(businessPageRepository, times(1)).findById(businessPageEntity.getId());
		verify(itemService, times(1)).updateFromBusiness(itemUpdateDto, businessPageEntity, itemEntity.getId());
		verify(businessPageRepository, times(1)).save(businessPageEntity);
	}

	@Test
	void deleteItemFromBusinessPage__success(){
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.adds(new ArrayList<>())
				.owner(userEntity)
				.build();
		businessPageEntity.setId(1L);

		ItemEntity itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.businessPage(businessPageEntity)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		businessPageEntity.getAdds().add(itemEntity);

		when(securityService.getCurrentUserId()).thenReturn(userEntity.getId());
		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));
		when(itemRepository.save(itemEntity)).thenReturn(itemEntity);

		ItemEntity deletedItem =
				businessPageService.deleteItemFromBusinessPage(businessPageEntity.getId(), itemEntity.getId());

		assertNotNull(deletedItem);
		assertEquals(Status.DELETED, deletedItem.getStatus());

		verify(securityService, times(1)).getCurrentUserId();
		verify(businessPageRepository, times(1)).findById(businessPageEntity.getId());
		verify(itemRepository, times(1)).save(itemEntity);
	}

	@Test
	void deleteItemFromBusinessPage__itemNotFound(){
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.adds(new ArrayList<>())
				.owner(userEntity)
				.build();
		businessPageEntity.setId(1L);

		ItemEntity itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.businessPage(businessPageEntity)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		when(securityService.getCurrentUserId()).thenReturn(userEntity.getId());
		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));

		assertThrows(NotFoundException.class,
				() -> businessPageService.deleteItemFromBusinessPage(businessPageEntity.getId(), itemEntity.getId()), ExceptionConstants.ITEM_NOT_FOUND.getString());

		verify(securityService, times(1)).getCurrentUserId();
		verify(businessPageRepository, times(1)).findById(businessPageEntity.getId());
		verify(itemRepository, times(0)).save(itemEntity);
	}

	@Test
	void deleteItemFromBusinessPage__wrongUserId(){
		UserEntity userEntity = new UserEntity();
		userEntity.setId("userId");

		BusinessPageEntity businessPageEntity = BusinessPageEntity.builder()
				.title("business page title")
				.description("business page description")
				.email("businessPage@Gmail.com")
				.address("business page address 2/5")
				.adds(new ArrayList<>())
				.owner(userEntity)
				.build();
		businessPageEntity.setId(1L);

		ItemEntity itemEntity = ItemEntity.builder()
				.title("New Item")
				.description("This is the new item for unit test")
				.status(Status.CREATED)
				.businessPage(businessPageEntity)
				.price(Price.builder()
						.price(new BigDecimal(1500))
						.currency(CurrencyEnum.USD)
						.build())
				.phoneNumbers(List.of("+37499999999"))
				.build();
		itemEntity.setId(1L);

		when(securityService.getCurrentUserId()).thenReturn("userId2");
		when(businessPageRepository.findById(businessPageEntity.getId())).thenReturn(Optional.of(businessPageEntity));

		assertThrows(SomethingWentWrongException.class,
				() -> businessPageService.deleteItemFromBusinessPage(businessPageEntity.getId(), itemEntity.getId()), ExceptionConstants.INVALID_ACTION.getString());

		verify(securityService, times(1)).getCurrentUserId();
		verify(businessPageRepository, times(1)).findById(businessPageEntity.getId());
		verify(itemRepository, times(0)).save(itemEntity);
	}
}
