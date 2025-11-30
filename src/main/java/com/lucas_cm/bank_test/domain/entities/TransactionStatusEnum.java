package com.lucas_cm.bank_test.domain.entities;

public enum TransactionStatusEnum {
    PENDING, CONFIRMED, REJECTED;

    static TransactionStatusEnum fromString(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "CONFIRMED" -> TransactionStatusEnum.CONFIRMED;
            case "REJECTED" -> TransactionStatusEnum.REJECTED;
            case "PENDING" -> TransactionStatusEnum.PENDING;
            default -> {
                throw new RuntimeException("");
            }
        };
    }
}
