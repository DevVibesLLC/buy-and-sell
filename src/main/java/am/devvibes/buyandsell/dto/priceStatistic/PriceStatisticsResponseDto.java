package am.devvibes.buyandsell.dto.priceStatistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
