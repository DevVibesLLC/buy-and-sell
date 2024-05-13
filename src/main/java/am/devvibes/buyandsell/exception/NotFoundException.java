package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.Constants;

public class NotFoundException extends RuntimeException {

	public NotFoundException(Constants message) {
		super(message.getString());
	}

}
