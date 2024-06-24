package am.devvibes.buyandsell.exception;

import am.devvibes.buyandsell.util.ExceptionConstants;

public class FileIsNullException extends RuntimeException {
    public FileIsNullException(ExceptionConstants message) {
        super(message.getString());
    }
}
