package ro.irisinfinity.events.exception;

import ro.irisinfinity.platform.common.exception.NotFoundException;

public class EventNotFoundException extends NotFoundException {

    public EventNotFoundException() {
        super("Event not found");
    }

    public EventNotFoundException(final String message) {
        super(message);
    }
}
