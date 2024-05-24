package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum FieldNameEnum {

	MARK("Mark"),
	MODEL("Model"),
	YEAR("Year"),
	MILEAGE("Mileage");

	private final String name;

	FieldNameEnum(String name) {
		this.name = name;
	}

}
