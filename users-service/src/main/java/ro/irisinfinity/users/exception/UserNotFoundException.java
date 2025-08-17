package ro.irisinfinity.users.exception;

import ro.irisinfinity.platform.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException() {
        super("User not found");
    }

    public UserNotFoundException(final String message) {
        super(message);
    }
}
