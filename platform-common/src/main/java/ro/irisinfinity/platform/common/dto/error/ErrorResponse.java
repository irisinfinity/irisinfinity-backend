package ro.irisinfinity.platform.common.dto.error;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
    Instant timestamp,
    HttpStatus status,
    Object error,
    String path
) {

}