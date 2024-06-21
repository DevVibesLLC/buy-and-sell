package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class SmartHomeAccessoriesFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String mark;

	private String type;

	private String condition;

}
