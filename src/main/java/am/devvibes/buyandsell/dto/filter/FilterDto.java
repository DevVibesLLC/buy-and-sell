package am.devvibes.buyandsell.dto.filter;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterDto {

	private String mark;
	private String model;

	private String startYear;
	private String endYear;

	private String bodyType;

	private String startPrice;
	private String endPrice;

	private Long currency;

	private Long country;
	private Long region;
	private Long city;

	private String startEngineSize;
	private String endEngineSize;

	private String transmission;

	private String driveType;

	private String startMileage;
	private String endMileage;

	private String steeringWheel;

	private String clearedCustom;

	private String color;

	private String wheelSize;

	private String headlights;

	private String interiorColor;

	private String exteriorColor;

	private String sunroof;

}