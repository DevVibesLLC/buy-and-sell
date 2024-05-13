package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.Constants;

public class SomethingWentWrong extends RuntimeException {

	public SomethingWentWrong(Constants message) {
		super(message.getString());
	}

}
