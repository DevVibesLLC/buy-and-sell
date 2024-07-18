package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class ShoesForBoysFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String season;

	private String shoeSize;

	private String condition;

}
