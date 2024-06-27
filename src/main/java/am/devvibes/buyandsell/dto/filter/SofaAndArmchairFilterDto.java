package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class SofaAndArmchairFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String upholstery;

	private String color;

	private String condition;

}