package com.micromart.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all Kafka events in MicroMart.
 * <p>
 * PEAA Pattern: Event (Domain Event)
 * Something that happened in the domain that domain experts care about.
 * <p>
 * All Kafka events should extend this class to ensure:
 * - Unique event ID for idempotency
 * - Timestamp for event ordering
 * - Event type for routing
 * - Source service identification
 *
 * @see <a href="https://martinfowler.com/eaaDev/DomainEvent.html">Domain Event</a>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this event instance.
     * Used for idempotency checking to prevent duplicate processing.
     */
    private String eventId;

    /**
     * Type of the event (e.g., "ORDER_CREATED", "INVENTORY_UPDATED").
     * Used for routing and filtering events.
     */
    private String eventType;

    /**
     * The service that produced this event.
     */
    private String source;

    /**
     * Timestamp when the event was created.
     */
    private LocalDateTime timestamp;

    /**
     * Version of the event schema for backward compatibility.
     */
    private int version;

    /**
     * Correlation ID for tracing related events across services.
     */
    private String correlationId;

    /**
     * Initialize the event with default values.
     * Call this from subclass constructors or use @SuperBuilder.
     */
    protected void initializeEvent(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.version = 1;
    }

    /**
     * Create a new correlation ID or use an existing one.
     *
     * @param existingCorrelationId Existing correlation ID to propagate, or null for new
     * @return The correlation ID
     */
    public String getOrCreateCorrelationId(String existingCorrelationId) {
        if (existingCorrelationId != null && !existingCorrelationId.isEmpty()) {
            this.correlationId = existingCorrelationId;
        } else if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
        return this.correlationId;
    }
}
