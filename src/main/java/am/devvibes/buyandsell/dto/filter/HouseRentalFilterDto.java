package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class HouseRentalFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

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

	private String amenities;

	private String facilities;

	private String serviceLines;

	private String startLandArea;
	private String endLandArea;

	private String withPets;
	private String withChildren;

	private String prepayment;

	private String utilityPayments;

}
