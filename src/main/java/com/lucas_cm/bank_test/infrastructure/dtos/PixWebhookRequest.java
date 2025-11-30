package com.lucas_cm.bank_test.infrastructure.dtos;

public record PixWebhookRequest(
        String endToEndId,
        String eventId,
        String eventType,
        String occurredAt
) {
}
