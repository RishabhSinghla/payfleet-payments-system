package com.payfleet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payfleet.dto.PaymentEvent;
import com.payfleet.model.EventStore;
import com.payfleet.model.PaymentStatus;
import com.payfleet.repository.EventStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Event Store Service - Manages persistent event storage and retrieval
 * <p>
 * Enterprise Pattern: Event Sourcing Implementation
 * Banking Context: Complete audit trail for regulatory compliance
 */
@Service
@Transactional
public class EventStoreService {

    private static final Logger logger = LoggerFactory.getLogger(EventStoreService.class);

    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventStoreService(EventStoreRepository eventStoreRepository,
                             ObjectMapper objectMapper) {
        this.eventStoreRepository = eventStoreRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Store Payment Event in Event Store
     */
    public EventStore storePaymentEvent(PaymentEvent paymentEvent, String userId, String correlationId) {
        try {
            // Determine aggregate ID based on payment reference
            String aggregateId = paymentEvent.getPaymentReference();
            String aggregateType = "Payment";

            // Get next version for this aggregate
            Long nextVersion = eventStoreRepository.getNextVersionForAggregate(aggregateId);

            // Serialize event data to JSON
            String eventData = objectMapper.writeValueAsString(paymentEvent);

            // Create metadata
            Map<String, Object> metadata = Map.of(
                    "source", "PaymentService",
                    "eventId", paymentEvent.getEventId(),
                    "timestamp", paymentEvent.getTimestamp().toString(),
                    "amount", paymentEvent.getAmount().toString(),
                    "currency", paymentEvent.getCurrency()
            );
            String metadataJson = objectMapper.writeValueAsString(metadata);

            // Create and store event
            EventStore eventStore = new EventStore(
                    aggregateId,
                    aggregateType,
                    paymentEvent.getEventType(),
                    eventData,
                    nextVersion,
                    userId,
                    correlationId
            );
            eventStore.setMetadata(metadataJson);

            EventStore savedEvent = eventStoreRepository.save(eventStore);

            logger.info("Stored event in event store: Type={}, Aggregate={}, Version={}",
                    paymentEvent.getEventType(), aggregateId, nextVersion);

            return savedEvent;

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize payment event: {}", e.getMessage(), e);
            throw new RuntimeException("Event serialization failed", e);
        } catch (Exception e) {
            logger.error("Failed to store event: {}", e.getMessage(), e);
            throw new RuntimeException("Event storage failed", e);
        }
    }

    /**
     * Get Event History for Payment
     */
    public List<EventStore> getPaymentEventHistory(String paymentReference) {
        return eventStoreRepository.findByAggregateIdOrderByVersionAsc(paymentReference);
    }

    /**
     * Replay Payment Events to Reconstruct State
     */
    public PaymentEvent replayPaymentEvents(String paymentReference) {
        List<EventStore> events = getPaymentEventHistory(paymentReference);

        if (events.isEmpty()) {
            throw new IllegalArgumentException("No events found for payment: " + paymentReference);
        }

        // Get the latest event to reconstruct current state
        EventStore latestEvent = events.get(events.size() - 1);

        try {
            PaymentEvent currentState = objectMapper.readValue(latestEvent.getEventData(), PaymentEvent.class);

            logger.info("Replayed {} events for payment: {}, Final state: {}",
                    events.size(), paymentReference, currentState.getStatus());

            return currentState;

        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize payment event: {}", e.getMessage(), e);
            throw new RuntimeException("Event deserialization failed", e);
        }
    }

    /**
     * Get Events by Type
     */
    public List<EventStore> getEventsByType(String eventType) {
        return eventStoreRepository.findByEventTypeOrderByCreatedAtAsc(eventType);
    }

    /**
     * Get Events by User
     */
    public List<EventStore> getEventsByUser(String userId) {
        return eventStoreRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    /**
     * Get Events by Date Range
     */
    public List<EventStore> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return eventStoreRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(startDate, endDate);
    }

    /**
     * Get Audit Trail for Regulatory Reporting
     */
    public List<EventStore> getAuditTrail(String aggregateType, String eventType,
                                          String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return eventStoreRepository.findEventsForAuditTrail(
                aggregateType, eventType, userId, startDate, endDate);
    }

    /**
     * Get Payment Timeline for Audit
     */
    public List<PaymentTimelineEntry> getPaymentTimeline(String paymentReference) {
        List<EventStore> events = getPaymentEventHistory(paymentReference);

        return events.stream().map(event -> {
            try {
                PaymentEvent paymentEvent = objectMapper.readValue(event.getEventData(), PaymentEvent.class);
                return new PaymentTimelineEntry(
                        event.getEventType(),
                        paymentEvent.getStatus(),
                        event.getCreatedAt(),
                        event.getUserId(),
                        paymentEvent.getAmount(),
                        paymentEvent.getProcessingDetails(),
                        paymentEvent.getFailureReason()
                );
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse event data for timeline: {}", e.getMessage());
                return new PaymentTimelineEntry(
                        event.getEventType(),
                        null,
                        event.getCreatedAt(),
                        event.getUserId(),
                        null,
                        "Event parsing failed",
                        null
                );
            }
        }).collect(Collectors.toList());
    }

    /**
     * Get Event Statistics
     */
    public EventStatistics getEventStatistics() {
        long totalEvents = eventStoreRepository.count();

        Map<String, Long> eventsByType = eventStoreRepository.findAll().stream()
                .collect(Collectors.groupingBy(EventStore::getEventType, Collectors.counting()));

        Map<String, Long> eventsByAggregateType = eventStoreRepository.findAll().stream()
                .collect(Collectors.groupingBy(EventStore::getAggregateType, Collectors.counting()));

        return new EventStatistics(totalEvents, eventsByType, eventsByAggregateType);
    }

    /**
     * Inner Classes for Response DTOs
     */
    public record PaymentTimelineEntry(String eventType, PaymentStatus paymentStatus, LocalDateTime timestamp,
                                       String userId, BigDecimal amount, String details, String failureReason) {
    }

    public record EventStatistics(long totalEvents, Map<String, Long> eventsByType,
                                  Map<String, Long> eventsByAggregateType) {
    }
}
