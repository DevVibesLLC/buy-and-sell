package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class EventVenueRentalFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String startFloorArea;
	private String endFloorArea;

	private String numberOfGuests;

	private String eventTypes;

	private String facilities;

	private String equipment;

	private String noiseAfterHours;

	private String withPets;

}