package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum Status {
	CREATED("Created", "Создано", "Ստեղծված"),
	DELETED("Deleted","Удалено","Հեռացված");

	private final String english;
	private final String russian;
	private final String armenian;

	Status(String english, String russian, String armenian) {
		this.english = english;
		this.russian = russian;
		this.armenian = armenian;
	}
}