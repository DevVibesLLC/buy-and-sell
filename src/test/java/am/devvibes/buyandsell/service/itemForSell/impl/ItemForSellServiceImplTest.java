package am.devvibes.buyandsell.service.itemForSell.impl;

import am.devvibes.buyandsell.BaseRepositoryTest;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.ItemForSellRepository;
import am.devvibes.buyandsell.service.configuration.ItemForSellTestConfiguration;
import am.devvibes.buyandsell.service.itemForSell.ItemForSellService;
import am.devvibes.buyandsell.util.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = ItemForSellTestConfiguration.class)
class ItemForSellServiceImplTest extends BaseRepositoryTest {

	@Autowired
	private ItemForSellRepository itemForSellRepository;

	@Autowired
	private ItemForSellService itemForSellService;

	@Test
	void saveItemForSell() {
		ItemForSellResponseDto itemForSellResponseDto = itemForSellService.saveItemForSell(
				ItemForSellRequestDto.builder()
						.name("Bmw")
						.category(Category.CAR)
						.price(175000.0)
						.description("new car")
						.quantity(1)
						.build());
		assertNotNull(itemForSellResponseDto);
		assertTrue(itemForSellRepository.existsById(itemForSellResponseDto.getId()));
	}

	@Test
	void findItemForSellById() {
		ItemForSellResponseDto itemForSellResponseDto = itemForSellService.saveItemForSell(
				ItemForSellRequestDto.builder()
						.name("Bmw")
						.category(Category.CAR)
						.price(175000.0)
						.description("new car")
						.quantity(1)
						.build());

		ItemForSellResponseDto itemForSellFindById =
				itemForSellService.findItemForSellById(itemForSellResponseDto.getId());
		assertNotNull(itemForSellFindById);
		assertEquals(itemForSellResponseDto.getId(), itemForSellFindById.getId());
	}

	@Test
	void findAllItemForSells() {
		itemForSellService.saveItemForSell(ItemForSellRequestDto.builder()
				.name("Bmw")
				.category(Category.CAR)
				.price(175000.0)
				.description("new car")
				.quantity(1)
				.build());

		itemForSellService.saveItemForSell(ItemForSellRequestDto.builder()
				.name("Bmw")
				.category(Category.CAR)
				.price(175000.0)
				.description("new car")
				.quantity(1)
				.build());

		List<ItemForSellResponseDto> allItemForSells = itemForSellService.findAllItemForSells();

		assertNotNull(allItemForSells);
		assertEquals(allItemForSells.size(), 2);
	}

	@Test
	void deleteItemForSell() {
		ItemForSellResponseDto itemForSellResponseDto = itemForSellService.saveItemForSell(
				ItemForSellRequestDto.builder()
						.name("Bmw")
						.category(Category.CAR)
						.price(175000.0)
						.description("new car")
						.quantity(1)
						.build());

		itemForSellService.deleteItemForSell(itemForSellResponseDto.getId());

		NotFoundException notFoundException = assertThrows(NotFoundException.class,
				() -> itemForSellService.findItemForSellById(itemForSellResponseDto.getId()));
		assertTrue(notFoundException.getMessage().contains("Item not found"));

	}

}