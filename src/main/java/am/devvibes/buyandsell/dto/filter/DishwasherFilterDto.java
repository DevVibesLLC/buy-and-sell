package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class DishwasherFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String condition;

}
