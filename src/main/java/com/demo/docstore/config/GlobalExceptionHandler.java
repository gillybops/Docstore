package com.demo.docstore.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — centralizes error response formatting.
 *
 * @RestControllerAdvice → applies to all @RestController classes in the app.
 * Without this, Spring would return a generic Whitelabel Error Page or a
 * raw stack trace. This class intercepts exceptions and returns clean,
 * structured JSON error responses instead.
 *
 * Two exception types handled:
 *   1. MethodArgumentNotValidException → triggered by @Valid failures
 *   2. Generic Exception → fallback for unexpected errors
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid validation failures.
     *
     * When a @NotBlank or @Size constraint fails on a @RequestBody field,
     * Spring throws MethodArgumentNotValidException. This handler collects
     * all field-level errors and returns them as a map.
     *
     * Example response:
     * {
     *   "status": 400,
     *   "errors": {
     *     "title": "Title is required",
     *     "content": "Content must not exceed 100,000 characters"
     *   },
     *   "timestamp": "2024-03-15T14:30:00"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Collect all field-level validation errors into a map
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "errors", fieldErrors,
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Fallback handler for any unexpected exception.
     * Returns a 500 with a safe message — never exposes the stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "message", "An unexpected error occurred",
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
