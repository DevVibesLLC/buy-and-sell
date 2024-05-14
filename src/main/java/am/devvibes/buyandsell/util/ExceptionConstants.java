package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum ExceptionConstants {

	USER_NOT_FOUND("User not found"),
	PASSWORDS_ARE_DIFFERENT("Password not muched"),
	USER_WITH_THIS_EMAIL_ALREADY_EXISTS("User with this email already exists"),
	PASSWORD_LENGTH_IS_LESS_THEN_8("Password length is less then 8"),
	INCORRECT_CODE("Incorrect code"),
	PRODUCT_NOT_FOUND("Product not found");


	private final String string;

	ExceptionConstants(String string) {
		this.string = string;
	}

}
