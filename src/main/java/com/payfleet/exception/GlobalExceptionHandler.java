package com.payfleet.exception;

import com.payfleet.dto.ErrorResponse;
import com.payfleet.dto.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for PayFleet APIs
 * <p>
 * Enterprise Pattern: Centralized Exception Handling
 * - Handles all exceptions across the application
 * - Provides consistent error response format
 * - Logs errors for monitoring and debugging
 * - Maps business exceptions to appropriate HTTP status codes
 * <p>
 * Banking Context: Critical for API reliability and debugging
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle PayFleet Business Logic Exceptions
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            InsufficientFundsException ex, WebRequest request) {

        logger.warn("Insufficient funds: Account={}, Requested={}, Available={}",
                ex.getAccountNumber(), ex.getRequestedAmount(), ex.getAvailableBalance());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode(),
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex, WebRequest request) {

        logger.warn("Account not found: {}", ex.getAccountNumber());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode(),
                HttpStatus.NOT_FOUND.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayment(
            DuplicatePaymentException ex, WebRequest request) {

        logger.warn("Duplicate payment detected: IdempotencyKey={}, ExistingPayment={}",
                ex.getIdempotencyKey(), ex.getExistingPaymentReference());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode(),
                HttpStatus.CONFLICT.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayment(
            InvalidPaymentException ex, WebRequest request) {

        logger.warn("Invalid payment: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode(),
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Bean Validation Errors (@Valid annotations)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Validation error: {}", ex.getMessage());

        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed",
                validationErrors,
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Constraint Violation Exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        logger.warn("Constraint violation: {}", ex.getMessage());

        List<ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(this::mapConstraintViolation)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed",
                validationErrors,
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Bind Exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, WebRequest request) {

        logger.warn("Bind exception: {}", ex.getMessage());

        List<ValidationError> validationErrors = ex.getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid request parameters",
                validationErrors,
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Type Mismatch Exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        logger.warn("Type mismatch: Parameter '{}' with value '{}' could not be converted to type '{}'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                "TYPE_MISMATCH",
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Authentication Errors
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        logger.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid username or password",
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Access Denied Errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        logger.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "Access denied. Insufficient permissions",
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Illegal Argument Exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        logger.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "INVALID_ARGUMENT",
                HttpStatus.BAD_REQUEST.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Generic Runtime Exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        logger.error("Runtime exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "An internal error occurred. Please try again later",
                "INTERNAL_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle All Other Exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "An unexpected error occurred. Please contact support",
                "UNEXPECTED_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper Methods
     */
    private ValidationError mapFieldError(FieldError fieldError) {
        return new ValidationError(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage()
        );
    }

    private ValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        return new ValidationError(
                violation.getPropertyPath().toString(),
                violation.getInvalidValue(),
                violation.getMessage()
        );
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
