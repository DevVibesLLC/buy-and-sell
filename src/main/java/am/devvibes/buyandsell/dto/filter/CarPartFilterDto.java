package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class CarPartFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String mark;

	private String originality;
	private String partSide;
	private String partPosition;

	private String condition;

}