package am.devvibes.buyandsell.dto.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewConstructionHouseFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String constructionType;

	private String floorsInTheBuilding;

	private String startHouseArea;
	private String endHouseArea;

	private String numberOfRooms;

	private String numberOfBathrooms;

	private String garage;

	private String serviceLines;

	private String startLandArea;
	private String endLandArea;

	private String interiorFinishing;

	private String mortgageIsPossible;

}