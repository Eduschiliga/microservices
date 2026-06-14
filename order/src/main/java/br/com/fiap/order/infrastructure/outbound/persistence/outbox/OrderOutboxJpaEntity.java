package br.com.fiap.order.infrastructure.outbound.persistence.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_outbox")
public class OrderOutboxJpaEntity {
    @Id
    private UUID id;
    private UUID aggregateId;
    private String eventType;
    private String topic;
    private String messageKey;
    @Column(columnDefinition = "text", nullable = false)
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    private int attempts;
    private Instant nextAttemptAt;
    @Column(columnDefinition = "text")
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;

    protected OrderOutboxJpaEntity() {
    }

    public OrderOutboxJpaEntity(UUID aggregateId, String eventType, String topic, String messageKey, String payload) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markPublished() {
        Instant now = Instant.now();
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.updatedAt = now;
        this.lastError = null;
    }

    public void markRetry(String error) {
        this.attempts++;
        this.lastError = error;
        this.nextAttemptAt = Instant.now().plusSeconds(Math.min(60, Math.max(1, attempts) * 5L));
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
