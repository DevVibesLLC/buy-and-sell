package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class CommercialBuyFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String constructionType;

	private String furniture;

	private String elevator;

	private String entrance;

	private String parking;

	private String startFloorArea;
	private String endFloorArea;

	private String locationFromTheStreet;

}
