package am.devvibes.buyandsell.dto.priceStatistic;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PriceStatisticsRequestDto {

	private String mark;
	private String model;
	private String startYear;
	private String endYear;

}
