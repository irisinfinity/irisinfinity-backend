package ro.irisinfinity.events.exception;

import ro.irisinfinity.platform.common.exception.AlreadyExistsException;

public class EventAlreadyExistsException extends AlreadyExistsException {

    public EventAlreadyExistsException() {
        super("Event already exists");
    }

    public EventAlreadyExistsException(final String message) {
        super(message);
    }
}