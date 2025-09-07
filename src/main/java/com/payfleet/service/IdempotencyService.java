package com.payfleet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payfleet.model.IdempotencyRecord;
import com.payfleet.model.IdempotencyStatus;
import com.payfleet.repository.IdempotencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Idempotency Service - Manages request deduplication and duplicate prevention
 * <p>
 * Enterprise Pattern: Idempotency Key Management
 * Banking Context: Prevents duplicate payment processing and ensures reliability
 */
@Service
@Transactional
public class IdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);
    private static final int DEFAULT_EXPIRY_HOURS = 24;
    private static final int PROCESSING_TIMEOUT_MINUTES = 10;

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public IdempotencyService(IdempotencyRepository idempotencyRepository,
                              ObjectMapper objectMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Check if request is idempotent and handle accordingly
     *
     * @param idempotencyKey Unique key for the request
     * @param resourceType   Type of resource being processed
     * @param userId         User making the request
     * @param requestPayload Request payload for hash validation
     * @return IdempotencyResult indicating how to proceed
     */
    public IdempotencyResult checkIdempotency(String idempotencyKey, String resourceType,
                                              String userId, Object requestPayload) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            logger.debug("No idempotency key provided, proceeding with normal processing");
            return IdempotencyResult.proceed();
        }

        // Calculate request hash for validation
        String requestHash = calculateRequestHash(requestPayload);

        // Check if idempotency record exists
        Optional<IdempotencyRecord> existingRecord =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isPresent()) {
            return handleExistingRecord(existingRecord.get(), requestHash, userId);
        } else {
            return createNewIdempotencyRecord(idempotencyKey, resourceType, userId, requestHash);
        }
    }

    /**
     * Handle existing idempotency record
     */
    private IdempotencyResult handleExistingRecord(IdempotencyRecord record, String requestHash, String userId) {

        // Validate request consistency
        if (!record.getUserId().equals(userId)) {
            logger.warn("Idempotency key {} used by different user. Original: {}, Current: {}",
                    record.getIdempotencyKey(), record.getUserId(), userId);
            return IdempotencyResult.conflict("Idempotency key belongs to different user");
        }

        if (!record.getRequestHash().equals(requestHash)) {
            logger.warn("Idempotency key {} used with different request payload", record.getIdempotencyKey());
            return IdempotencyResult.conflict("Request payload differs from original");
        }

        // Check if record is expired
        if (record.isExpired()) {
            logger.info("Idempotency record {} expired, removing and proceeding", record.getIdempotencyKey());
            idempotencyRepository.delete(record);
            return createNewIdempotencyRecord(record.getIdempotencyKey(), record.getResourceType(),
                    userId, requestHash);
        }

        // Handle based on current status
        switch (record.getStatus()) {
            case PROCESSING:
                if (record.isProcessingTimedOut(PROCESSING_TIMEOUT_MINUTES)) {
                    logger.warn("Idempotency record {} processing timed out, resetting", record.getIdempotencyKey());
                    record.setStatus(IdempotencyStatus.TIMEOUT);
                    record.setProcessingStartedAt(LocalDateTime.now());
                    idempotencyRepository.save(record);
                    return IdempotencyResult.proceed(record);
                } else {
                    logger.info("Request {} still processing, returning processing status", record.getIdempotencyKey());
                    return IdempotencyResult.processing();
                }

            case COMPLETED:
                logger.info("Request {} already completed, returning cached response", record.getIdempotencyKey());
                return IdempotencyResult.duplicate(record.getResponseBody(), record.getHttpStatus());

            case FAILED:
                logger.info("Previous request {} failed, allowing retry", record.getIdempotencyKey());
                record.setStatus(IdempotencyStatus.PROCESSING);
                record.setProcessingStartedAt(LocalDateTime.now());
                idempotencyRepository.save(record);
                return IdempotencyResult.proceed(record);

            case TIMEOUT:
                logger.info("Previous request {} timed out, allowing retry", record.getIdempotencyKey());
                record.setStatus(IdempotencyStatus.PROCESSING);
                record.setProcessingStartedAt(LocalDateTime.now());
                idempotencyRepository.save(record);
                return IdempotencyResult.proceed(record);

            default:
                logger.warn("Unknown idempotency status: {}", record.getStatus());
                return IdempotencyResult.proceed(record);
        }
    }

    /**
     * Create new idempotency record
     */
    private IdempotencyResult createNewIdempotencyRecord(String idempotencyKey, String resourceType,
                                                         String userId, String requestHash) {

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(DEFAULT_EXPIRY_HOURS);

        IdempotencyRecord record = new IdempotencyRecord(
                idempotencyKey, resourceType, userId, requestHash, expiresAt);

        try {
            IdempotencyRecord savedRecord = idempotencyRepository.save(record);
            logger.info("Created new idempotency record: {}", idempotencyKey);
            return IdempotencyResult.proceed(savedRecord);

        } catch (Exception e) {
            // Handle race condition where another thread created the record
            logger.warn("Failed to create idempotency record, checking if it exists: {}", e.getMessage());
            Optional<IdempotencyRecord> existingRecord =
                    idempotencyRepository.findByIdempotencyKey(idempotencyKey);

            if (existingRecord.isPresent()) {
                return handleExistingRecord(existingRecord.get(), requestHash, userId);
            } else {
                throw new RuntimeException("Failed to create idempotency record", e);
            }
        }
    }

    /**
     * Mark idempotency request as completed
     */
    public void markCompleted(String idempotencyKey, String resourceId, String responseBody, int httpStatus) {
        Optional<IdempotencyRecord> recordOptional =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (recordOptional.isPresent()) {
            IdempotencyRecord record = recordOptional.get();
            record.setStatus(IdempotencyStatus.COMPLETED);
            record.setResourceId(resourceId);
            record.setResponseBody(responseBody);
            record.setHttpStatus(httpStatus);
            idempotencyRepository.save(record);

            logger.info("Marked idempotency record {} as completed", idempotencyKey);
        } else {
            logger.warn("Idempotency record not found for key: {}", idempotencyKey);
        }
    }

    /**
     * Mark idempotency request as failed
     */
    public void markFailed(String idempotencyKey, String errorMessage, int httpStatus) {
        Optional<IdempotencyRecord> recordOptional =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (recordOptional.isPresent()) {
            IdempotencyRecord record = recordOptional.get();
            record.setStatus(IdempotencyStatus.FAILED);
            record.setResponseBody(errorMessage);
            record.setHttpStatus(httpStatus);
            idempotencyRepository.save(record);

            logger.info("Marked idempotency record {} as failed", idempotencyKey);
        } else {
            logger.warn("Idempotency record not found for key: {}", idempotencyKey);
        }
    }

    /**
     * Calculate hash of request payload for validation
     */
    private String calculateRequestHash(Object requestPayload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(requestPayload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(jsonPayload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            logger.error("Failed to calculate request hash: {}", e.getMessage(), e);
            throw new RuntimeException("Request hash calculation failed", e);
        }
    }

    /**
     * Clean up expired idempotency records
     */
    @Transactional
    public int cleanupExpiredRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<IdempotencyRecord> expiredRecords = idempotencyRepository.findExpiredRecords(now);

        if (!expiredRecords.isEmpty()) {
            idempotencyRepository.deleteAll(expiredRecords);
            logger.info("Cleaned up {} expired idempotency records", expiredRecords.size());
        }

        return expiredRecords.size();
    }

    /**
     * Get idempotency statistics
     */
    public IdempotencyStatistics getStatistics() {
        long totalRecords = idempotencyRepository.count();
        long processingRecords = idempotencyRepository.countByStatus(IdempotencyStatus.PROCESSING);
        long completedRecords = idempotencyRepository.countByStatus(IdempotencyStatus.COMPLETED);
        long failedRecords = idempotencyRepository.countByStatus(IdempotencyStatus.FAILED);

        return new IdempotencyStatistics(totalRecords, processingRecords, completedRecords, failedRecords);
    }

    public enum IdempotencyAction {
        PROCEED, RETURN_DUPLICATE, PROCESSING, CONFLICT
    }

    /**
     * Inner Classes for Response Objects
     */
    public static class IdempotencyResult {
        private final IdempotencyAction action;
        private final String message;
        private final String responseBody;
        private final Integer httpStatus;
        private final IdempotencyRecord record;

        private IdempotencyResult(IdempotencyAction action, String message,
                                  String responseBody, Integer httpStatus, IdempotencyRecord record) {
            this.action = action;
            this.message = message;
            this.responseBody = responseBody;
            this.httpStatus = httpStatus;
            this.record = record;
        }

        public static IdempotencyResult proceed() {
            return new IdempotencyResult(IdempotencyAction.PROCEED, null, null, null, null);
        }

        public static IdempotencyResult proceed(IdempotencyRecord record) {
            return new IdempotencyResult(IdempotencyAction.PROCEED, null, null, null, record);
        }

        public static IdempotencyResult duplicate(String responseBody, int httpStatus) {
            return new IdempotencyResult(IdempotencyAction.RETURN_DUPLICATE, null, responseBody, httpStatus, null);
        }

        public static IdempotencyResult processing() {
            return new IdempotencyResult(IdempotencyAction.PROCESSING, "Request is still processing", null, 202, null);
        }

        public static IdempotencyResult conflict(String message) {
            return new IdempotencyResult(IdempotencyAction.CONFLICT, message, null, 409, null);
        }

        // Getters
        public IdempotencyAction getAction() {
            return action;
        }

        public String getMessage() {
            return message;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public Integer getHttpStatus() {
            return httpStatus;
        }

        public IdempotencyRecord getRecord() {
            return record;
        }
    }

    public record IdempotencyStatistics(long totalRecords, long processingRecords, long completedRecords,
                                        long failedRecords) {
    }
}
