package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.ExceptionConstants;

public class VerificationException extends RuntimeException {

	public VerificationException(ExceptionConstants message) {
		super(message.getString());
	}

}
