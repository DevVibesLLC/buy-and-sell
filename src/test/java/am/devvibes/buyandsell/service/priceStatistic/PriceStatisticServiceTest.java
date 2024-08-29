package am.devvibes.buyandsell.service.priceStatistic;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsRequestDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsResponseDto;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.priceStatistic.impl.PriceStatisticServiceImpl;
import am.devvibes.buyandsell.util.ExceptionConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceStatisticServiceTest {

	@Mock
	private ItemService itemService;

	@Mock
	private ItemMapper itemMapper;

	@InjectMocks
	private PriceStatisticServiceImpl priceStatisticService;

	@Test
	void getPriceStatistic__success() {

		PriceStatisticsRequestDto requestDto = new PriceStatisticsRequestDto();

		ItemEntity itemEntity1 =
				ItemEntity.builder().price(Price.builder().price(new BigDecimal("100.0")).build()).build();
		ItemEntity itemEntity2 =
				ItemEntity.builder().price(Price.builder().price(new BigDecimal("200.0")).build()).build();
		ItemEntity itemEntity3 =
				ItemEntity.builder().price(Price.builder().price(new BigDecimal("150.0")).build()).build();

		List<ItemEntity> itemEntities = Arrays.asList(itemEntity1, itemEntity2, itemEntity3);

		ItemResponseDto itemResponseDto1 =
				ItemResponseDto.builder().price(Price.builder().price(new BigDecimal("100.0")).build()).build();
		ItemResponseDto itemResponseDto2 =
				ItemResponseDto.builder().price(Price.builder().price(new BigDecimal("200.0")).build()).build();
		ItemResponseDto itemResponseDto3 =
				ItemResponseDto.builder().price(Price.builder().price(new BigDecimal("150.0")).build()).build();

		List<ItemResponseDto> itemResponseDtos = Arrays.asList(itemResponseDto1, itemResponseDto2, itemResponseDto3);

		when(itemService.filterItems(requestDto)).thenReturn(itemEntities);
		when(itemMapper.mapEntityListToDtoList(itemEntities)).thenReturn(itemResponseDtos);

		PriceStatisticsResponseDto response = priceStatisticService.getPriceStatistic(requestDto);

		assertNotNull(response);
		assertEquals(new BigDecimal("100.0"), response.getMinPrice());
		assertEquals(new BigDecimal("200.0"), response.getMaxPrice());
		assertEquals(new BigDecimal("150.0"), response.getAvgPrice());

		verify(itemService, times(1)).filterItems(requestDto);
		verify(itemMapper, times(1)).mapEntityListToDtoList(itemEntities);

	}

	@Test
	void getPriceStatistic_valueNotFound() {

		PriceStatisticsRequestDto priceStatisticsRequestDto = new PriceStatisticsRequestDto();
		List<ItemEntity> list = List.of();

		when(itemService.filterItems(priceStatisticsRequestDto)).thenReturn(list);
		when(itemMapper.mapEntityListToDtoList(list)).thenReturn(Collections.emptyList());

		assertThrows(NotFoundException.class,
				() -> priceStatisticService.getPriceStatistic(priceStatisticsRequestDto), ExceptionConstants.VALUE_NOT_FOUND.getString());

		verify(itemService, times(1)).filterItems(priceStatisticsRequestDto);
		verify(itemMapper, times(1)).mapEntityListToDtoList(anyList());
	}

}
