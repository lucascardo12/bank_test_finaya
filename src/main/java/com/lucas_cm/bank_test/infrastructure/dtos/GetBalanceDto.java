package com.lucas_cm.bank_test.infrastructure.dtos;

import java.math.BigDecimal;

public record GetBalanceDto(
        String walletId,
        BigDecimal currentBalance
) {
}
