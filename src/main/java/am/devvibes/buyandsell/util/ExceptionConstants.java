package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum ExceptionConstants {

	USER_NOT_FOUND("User not found"),
	PASSWORDS_ARE_DIFFERENT("Password not muched"),
	USER_WITH_THIS_EMAIL_ALREADY_EXISTS("User with this email already exists"),
	USER_WITH_THIS_USERNAME_ALREADY_EXISTS("User with this username already exists"),
	PASSWORD_LENGTH_MUST_BE_MORE_THEN_8("Password length must be more then 8"),
	INVALID_ACTION("Invalid action"),
	INCORRECT_CODE("Incorrect code"),
	ITEM_NOT_FOUND("Item not found"),
	CATEGORY_NOT_FOUND("Category not found");


	private final String string;

	ExceptionConstants(String string) {
		this.string = string;
	}

}
