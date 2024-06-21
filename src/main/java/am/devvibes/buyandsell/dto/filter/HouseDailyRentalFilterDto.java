package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class HouseDailyRentalFilterDto {

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

	private String garage;

	private String renovation;

	private String comfort;

	private String amenities;

	private String appliances;

	private String startLandArea;
	private String endLandArea;

	private String withPets;

	private String withChildren;

	private String numberOfGuests;

}
