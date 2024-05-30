package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.ExceptionConstants;

public class PageRequestValidationException extends RuntimeException {
    public PageRequestValidationException(ExceptionConstants message) {
        super(message.getString());
    }
}
