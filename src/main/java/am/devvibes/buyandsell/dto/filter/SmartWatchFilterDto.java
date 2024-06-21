package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class SmartWatchFilterDto {

	private String mark;

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String condition;

	private String color;

}
