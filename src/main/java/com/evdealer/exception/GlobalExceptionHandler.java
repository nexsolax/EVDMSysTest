package com.evdealer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler để chuẩn hoá exception handling cho toàn bộ application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle RuntimeException - thường là business logic errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        logger.error("RuntimeException occurred: {}", e.getMessage(), e);
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        
        // Phân loại lỗi dựa trên message
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("not found") || message.contains("Not found")) {
                error.put("status", HttpStatus.NOT_FOUND.value());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            if (message.contains("already exists") || message.contains("duplicate") || message.contains("unique")) {
                error.put("status", HttpStatus.CONFLICT.value());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            if (message.contains("Access denied") || message.contains("Forbidden")) {
                error.put("status", HttpStatus.FORBIDDEN.value());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            if (message.contains("Authentication required") || message.contains("Unauthorized")) {
                error.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle IllegalArgumentException - validation errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("IllegalArgumentException occurred: {}", e.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle MethodArgumentNotValidException - validation errors từ @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        logger.warn("Validation error: {}", e.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Validation failed");
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        
        Map<String, String> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                    (existing, replacement) -> existing
                ));
        
        error.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle MethodArgumentTypeMismatchException - type conversion errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.warn("Type mismatch error: {}", e.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        Class<?> requiredType = e.getRequiredType();
        String requiredTypeName = requiredType != null ? requiredType.getSimpleName() : "unknown";
        error.put("error", "Invalid parameter type: " + e.getName() + " should be " + requiredTypeName);
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle generic Exception - catch-all for unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred: {}", e.getMessage(), e);
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "An unexpected error occurred. Please contact support.");
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        // Chỉ log chi tiết, không trả về client
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

