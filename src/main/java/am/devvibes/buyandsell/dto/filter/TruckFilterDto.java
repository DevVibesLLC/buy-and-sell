package am.devvibes.buyandsell.dto.filter;

import lombok.*;

@Getter
@Setter
@Builder
public class TruckFilterDto {

	private String mark;
	private String model;

	private String startYear;
	private String endYear;

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String chassiesConfiguration;

	private String steeringWheel;

	private String transmission;

	private String engineType;

	private String startMileage;
	private String endMileage;

	private String clearedCustom;

	private String color;
}
