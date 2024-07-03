package am.devvibes.buyandsell.dto.priceStatistic;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class PriceStatisticsResponseDto {

	BigDecimal minPrice;
	BigDecimal maxPrice;
	BigDecimal avgPrice;

}
