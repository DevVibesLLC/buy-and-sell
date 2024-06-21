package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class TabletFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String mark;

	private String condition;

	private String screenSize;

	private String memory;

	private String color;

}