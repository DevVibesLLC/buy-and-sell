package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class WatchesFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String type;

	private String gender;

	private String clockFace;

	private String color;

	private String condition;

}
