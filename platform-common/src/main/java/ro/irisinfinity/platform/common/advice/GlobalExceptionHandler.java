package ro.irisinfinity.platform.common.advice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import ro.irisinfinity.platform.common.dto.error.ErrorResponse;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex,
        HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }

        try {
            var feignReq = ex.request();
            log.warn("Downstream error {} from {} {} -> handling for {}",
                ex.status(),
                feignReq != null ? feignReq.httpMethod() : "?",
                feignReq != null ? feignReq.url() : "?",
                request.getRequestURI(),
                ex);
        } catch (Throwable ignored) { /* no-op */ }

        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status,
            extractFeignPayload(ex),
            request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex,
        HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status,
            ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
        HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status,
            ex.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
        HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                DefaultMessageSourceResolvable::getDefaultMessage,
                (a, b) -> a,
                LinkedHashMap::new));
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status,
            errors,
            request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
        HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "?";

        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status,
            "Invalid value '%s' for parameter '%s'. Expected type: %s".formatted(ex.getValue(),
                ex.getName(), expected),
            request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.toString(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status,
            "An unexpected error occurred",
            request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    private Object extractFeignPayload(FeignException ex) {
        String body = readBody(ex);
        if (body == null || body.isBlank()) {
            return ex.getMessage();
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode errorNode = root.has("error") ? root.get("error") : root;
            return objectMapper.convertValue(errorNode, Map.class);
        } catch (Exception ignore) {
            return body.trim();
        }
    }

    private String readBody(FeignException ex) {
        try {
            if (ex.responseBody().isPresent()) {
                ByteBuffer buf = ex.responseBody().get().asReadOnlyBuffer();
                byte[] bytes = new byte[buf.remaining()];
                buf.get(bytes);
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Throwable ignored) { /* no-op */ }
        try {
            return ex.contentUTF8();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
