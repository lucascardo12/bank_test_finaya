package com.lucas_cm.bank_test.infrastructure.dtos;

import com.lucas_cm.bank_test.domain.entities.TransactionStatusEnum;

public record PixTransferResponse(
        String endToEndId,
        TransactionStatusEnum status
) {
}
