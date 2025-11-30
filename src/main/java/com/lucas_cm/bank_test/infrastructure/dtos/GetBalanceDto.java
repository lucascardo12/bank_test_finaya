package com.lucas_cm.bank_test.infrastructure.dtos;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;

import java.util.List;

public record GetBalanceDto(
        String walletId,
        double currentBalance,
        List<TransactionEntity> transactions
) {
}
