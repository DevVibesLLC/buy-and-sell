package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class WasherFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String mark;

	private String type;

	private String maximumLaundryCapacity;

	private String laundryLoadType;

	private String condition;
}
