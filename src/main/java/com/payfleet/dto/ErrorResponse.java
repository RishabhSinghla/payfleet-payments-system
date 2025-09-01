package com.payfleet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized Error Response DTO
 * <p>
 * Enterprise Pattern: Consistent API Error Format
 * - Provides structured error information to API consumers
 * - Enables consistent error handling across frontend applications
 * - Supports multiple validation errors in a single response
 * <p>
 * Banking Context: Critical for API reliability and debugging
 */
public class ErrorResponse {

    private boolean success = false;
    private String message;
    private String errorCode;
    private int httpStatus;
    private String path;
    private List<ValidationError> validationErrors;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Default constructor
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for simple errors
    public ErrorResponse(String message, String errorCode, int httpStatus, String path) {
        this();
        this.message = message;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.path = path;
    }

    // Constructor for validation errors
    public ErrorResponse(String message, List<ValidationError> validationErrors,
                         int httpStatus, String path) {
        this();
        this.message = message;
        this.validationErrors = validationErrors;
        this.httpStatus = httpStatus;
        this.path = path;
        this.errorCode = "VALIDATION_ERROR";
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
