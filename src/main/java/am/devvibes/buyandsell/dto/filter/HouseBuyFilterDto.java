package am.devvibes.buyandsell.dto.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HouseBuyFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String condition;

	private String constructionType;

	private String startHouseArea;
	private String endHouseArea;

	private String floorsInTheBuilding;

	private String numberOfRooms;

	private String numberOfBathrooms;

	private String furniture;

	private String renovation;

	private String garage;

	private String appliances;

	private String facilities;

	private String serviceLines;

	private String startLandArea;
	private String endLandArea;
}
