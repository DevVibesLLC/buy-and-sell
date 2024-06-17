package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum DescriptionNameEnum {

	SPECIFICATIONS("Specifications"),
	EXTERIOR("Exterior"),
	INTERIOR("Interior"),
	ADDITIONAL_INFORMATION("Additional Information"),
	BUILDING_INFORMATION("Building Information"),
	APARTMENT_INFORMATION("Apartment Information"),
	HOUSE_RULES("House Rules"),
	DEAL_TERMS("Deal Terms"),
	HOUSE_INFORMATION("House Information"),
	LOT_INFORMATION("Lot Information"),
	STAGE_OF_PREPARATION("Stage of Preparation");
	private final String name;

	DescriptionNameEnum(String name) {
		this.name = name;
	}

}
