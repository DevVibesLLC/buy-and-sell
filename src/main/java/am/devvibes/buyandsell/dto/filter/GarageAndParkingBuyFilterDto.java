package am.devvibes.buyandsell.dto.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GarageAndParkingBuyFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String startFloorArea;
	private String endFloorArea;

	private String amenities;

	private String utilities;

	private String type;

}
