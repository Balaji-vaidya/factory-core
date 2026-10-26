package com.factory.common.exception;

import com.factory.common.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Business exception [{}] [{}]: {}", correlationId, ex.getErrorCode().name(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode().getHttpStatus().value(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                correlationId
        );
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Validation error [{}]: {}", correlationId, ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_FAILED.name(),
                "Validation failed for one or more fields.",
                correlationId
        );

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            response.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Illegal argument [{}]: {}", correlationId, ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_REQUEST.name(),
                ex.getMessage(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Unhandled exception [{}]: ", correlationId, ex);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "An unexpected internal error occurred. Please contact system administrator with correlation ID.",
                correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
