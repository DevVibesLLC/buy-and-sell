package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum ExceptionConstants {

	USER_NOT_FOUND("User not found"),
	PASSWORDS_ARE_DIFFERENT("Passwords not muched"),
	USER_WITH_THAT_EMAIL_ALREADY_EXISTS("User with that email already exists"),
	USER_WITH_THAT_USERNAME_ALREADY_EXISTS("User with that username already exists"),
	PASSWORD_LENGTH_MUST_BE_MORE_THEN_8("Password length must be more then 8"),
	INVALID_ACTION("Invalid action"),
	INCORRECT_CODE("Incorrect code"),
	ITEM_NOT_FOUND("Item not found"),
	CATEGORY_NOT_FOUND("Category not found"),
	LOCATION_NOT_FOUND("Location not found");


	private final String string;

	ExceptionConstants(String string) {
		this.string = string;
	}

}
