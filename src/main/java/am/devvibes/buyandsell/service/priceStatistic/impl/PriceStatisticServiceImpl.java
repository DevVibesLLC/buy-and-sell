package am.devvibes.buyandsell.service.priceStatistic.impl;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsRequestDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsResponseDto;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.priceStatistic.PriceStatisticService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceStatisticServiceImpl implements PriceStatisticService {

	private final ItemService itemService;
	private final ItemMapper itemMapper;

	@Override
	public PriceStatisticsResponseDto getPriceStatistic(PriceStatisticsRequestDto priceStatisticsRequestDto) {
		List<ItemEntity> itemEntities = itemService.filterItems(priceStatisticsRequestDto);
		List<ItemResponseDto> itemResponseDtos = itemMapper.mapEntityListToDtoList(itemEntities);
		BigDecimal minPrice = itemResponseDtos.stream()
				.map(ItemResponseDto::getPrice)
				.min(Comparator.comparingInt(item -> item.getPrice().intValue()))
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.VALUE_NOT_FOUND))
				.getPrice();

		BigDecimal maxPrice = itemResponseDtos.stream()
				.map(ItemResponseDto::getPrice)
				.max(Comparator.comparingInt(item -> item.getPrice().intValue()))
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.VALUE_NOT_FOUND))
				.getPrice();

		BigDecimal average = BigDecimal.valueOf((minPrice.doubleValue() + maxPrice.doubleValue()) / 2);

		return PriceStatisticsResponseDto.builder().minPrice(minPrice).maxPrice(maxPrice).avgPrice(average).build();
	}

}
