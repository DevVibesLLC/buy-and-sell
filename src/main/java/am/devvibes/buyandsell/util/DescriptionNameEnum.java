package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum DescriptionNameEnum {

	SPECIFICATIONS("Specifications"),
	EXTERIOR("Exterior"),
	INTERIOR("Interior");

	private final String name;

	DescriptionNameEnum(String name) {
		this.name = name;
	}

}
