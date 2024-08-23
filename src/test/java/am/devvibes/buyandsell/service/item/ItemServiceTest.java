package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.item.ItemUpdateDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.priceHistory.PriceHistoryEntity;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.repository.priceHistory.PriceHistoryRepository;
import am.devvibes.buyandsell.service.item.impl.ItemServiceImpl;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.util.CurrencyEnum;
import am.devvibes.buyandsell.util.Status;
import am.devvibes.buyandsell.util.page.CustomPageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	private ValueService valueService;

	@Mock
	private SecurityService securityService;

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

		when(itemRepository.findById(result.getId())).thenReturn(Optional.of(result));

		ItemEntity updatedEntity = itemService.updateFromBusiness(itemUpdateDto, businessPage, result.getId());

		assertNotNull(updatedEntity);
		assertNotEquals(itemRequestDto.getTitle(), updatedEntity.getTitle());
		assertEquals(result.getDescription(), updatedEntity.getDescription());

		verify(itemMapper, times(1)).mapDtoToEntityFromBusiness(itemRequestDto, businessPage);
		verify(itemRepository, times(2)).save(itemEntity);

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

}