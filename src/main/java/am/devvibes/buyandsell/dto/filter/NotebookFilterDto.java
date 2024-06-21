package am.devvibes.buyandsell.dto.filter;

import lombok.Getter;

@Getter
public class NotebookFilterDto {

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String mark;

	private String condition;

	private String processor;

	private String memoryRAM;

	private String memory;

	private String screenResolution;

	private String screenSize;

}
