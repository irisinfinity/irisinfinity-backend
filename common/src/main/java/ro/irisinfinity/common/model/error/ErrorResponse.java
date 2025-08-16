package ro.irisinfinity.common.model.error;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        HttpStatus status,
        String error,
        Object message,
        String path
) {}
