package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.ExceptionConstants;

public class UnsupportedExtensionException extends RuntimeException {
    public UnsupportedExtensionException(ExceptionConstants message) {
        super(message.getString());
    }
}
