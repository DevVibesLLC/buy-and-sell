package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum CategoryEnum {

	CAR("Car"),
	TRUCK("Truck"),
	BUS("Bus");

	private final String name;

	CategoryEnum(String name) {
		this.name = name;
	}

}
