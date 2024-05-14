package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.ExceptionConstants;

public class SomethingWentWrongException extends RuntimeException {

	public SomethingWentWrongException(ExceptionConstants message) {
		super(message.getString());
	}

}
