package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class MobilePhoneFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String mark;

	private String model;

	private String condition;

	private String storage;

	private String color;

}