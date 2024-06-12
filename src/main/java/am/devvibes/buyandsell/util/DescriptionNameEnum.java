package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum DescriptionNameEnum {

	SPECIFICATIONS("Specifications"),
	EXTERIOR("Exterior"),
	INTERIOR("Interior"),
	ADDITIONAL_INFORMATION("Additional Information");

	private final String name;

	DescriptionNameEnum(String name) {
		this.name = name;
	}

}
