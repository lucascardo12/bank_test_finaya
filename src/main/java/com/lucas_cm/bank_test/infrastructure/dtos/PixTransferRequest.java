package com.lucas_cm.bank_test.infrastructure.dtos;

import java.math.BigDecimal;

public record PixTransferRequest(
        String fromWalletId,
        String toPixKey,
        BigDecimal amount
) {
}
