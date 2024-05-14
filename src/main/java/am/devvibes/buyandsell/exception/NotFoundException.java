package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.ExceptionConstants;

public class NotFoundException extends RuntimeException {

	public NotFoundException(ExceptionConstants message) {
		super(message.getString());
	}

}
