package com.andara.domain.content.events;

import com.andara.content.ContentType;
import com.andara.domain.DomainEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain event emitted when content is hot-reloaded in development.
 */
public record ContentReloaded(
    UUID eventId,
    String eventType,
    Instant timestamp,
    String aggregateId,
    String aggregateType,
    long version,
    Map<String, Object> payload,
    Map<String, String> metadata
) implements DomainEvent {

    public static ContentReloaded create(
        ContentType contentType,
        List<String> reloadedIds,
        String source,
        UUID instanceId,
        UUID agentId
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contentType", contentType.name());
        payload.put("reloadedIds", reloadedIds);
        payload.put("source", source);
        payload.put("count", reloadedIds.size());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("instanceId", instanceId != null ? instanceId.toString() : "system");
        metadata.put("agentId", agentId != null ? agentId.toString() : "system");

        return new ContentReloaded(
            UUID.randomUUID(),
            "ContentReloaded",
            Instant.now(),
            contentType.name(),
            "Content",
            1L,
            payload,
            metadata
        );
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getAggregateType() {
        return aggregateType;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Map<String, Object> getPayload() {
        return payload;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public ContentType getContentType() {
        return ContentType.valueOf((String) payload.get("contentType"));
    }

    @SuppressWarnings("unchecked")
    public List<String> getReloadedIds() {
        return (List<String>) payload.get("reloadedIds");
    }
}
