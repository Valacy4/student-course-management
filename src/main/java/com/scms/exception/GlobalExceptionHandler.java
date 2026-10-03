package com.scms.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid failures -> 400 with field-wise messages
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Validation failed", "fields", fields));
    }

    // Bad JSON / invalid date format -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid request body (check the date format yyyy-MM-dd)"));
    }

    // 404 / 409 thrown from controllers
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error", String.valueOf(ex.getReason())));
    }
}