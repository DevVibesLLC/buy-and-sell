package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum CategoryEnum {

	CAR("Car"),
	HEALTH("Health"),
	HOME("Home");

	private final String name;

	CategoryEnum(String name) {
		this.name = name;
	}

}
