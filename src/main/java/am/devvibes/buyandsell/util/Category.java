package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum Category {

	FOOD("food"),
	CAR("car");


	private final String string;

	Category(String string) {
		this.string = string;
	}

}
