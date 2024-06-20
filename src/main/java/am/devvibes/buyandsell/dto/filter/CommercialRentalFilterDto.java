package am.devvibes.buyandsell.dto.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommercialRentalFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String furniture;

	private String elevator;

	private String entrance;

	private String parking;

	private String startFloorArea;
	private String endFloorArea;

	private String locationFromTheStreet;

	private String prepayment;

	private String utilityPayments;

	private String minimumRentalPeriod;

	private String leaseType;

}
