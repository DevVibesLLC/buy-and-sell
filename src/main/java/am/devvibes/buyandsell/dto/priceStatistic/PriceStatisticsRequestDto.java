package am.devvibes.buyandsell.dto.priceStatistic;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PriceStatisticsRequestDto {

	@NotBlank
	private String mark;

	@NotBlank
	private String model;

	@NotBlank
	private String startYear;

	@NotBlank
	private String endYear;

}
