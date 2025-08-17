package ro.irisinfinity.users.exception;

import ro.irisinfinity.platform.common.exception.AlreadyExistsException;

public class UserAlreadyExistsException extends AlreadyExistsException {

    public UserAlreadyExistsException() {
        super("User already exists");
    }

    public UserAlreadyExistsException(final String message) {
        super(message);
    }
}
