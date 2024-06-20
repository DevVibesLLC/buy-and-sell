package am.devvibes.buyandsell.dto.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApartmentBuyFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String constructionType;
	private String newConstruction;

	private String elevator;

	private String floorsInTheBuilding;

	private String theHouseHas;

	private String parking;

	private String startFloorArea;
	private String endFloorArea;

	private String numberOfRooms;

	private String numberOfBathrooms;

	private String ceilingHeight;

	private String startFloor;
	private String endFloor;

	private String balcony;

	private String furniture;

	private String renovation;

	private String appliances;

	private String windowViews;
}
