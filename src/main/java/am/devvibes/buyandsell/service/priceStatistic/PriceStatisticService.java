package am.devvibes.buyandsell.service.priceStatistic;

import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsRequestDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsResponseDto;

public interface PriceStatisticService {

	PriceStatisticsResponseDto getPriceStatistic(PriceStatisticsRequestDto priceStatisticsRequestDto);

}
