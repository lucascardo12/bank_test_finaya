package com.lucas_cm.bank_test.domain.exceptions;

import com.lucas_cm.bank_test.configuration.exception.BusinessException;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class InsufficientBalanceException extends BusinessException {
    private BigDecimal currentBalance;

    @Override
    public String getMessage() {
        return "Saldo insuficiente para realizar esta operação. Saldo atual " + currentBalance;
    }

    @Override
    public String getErrorCode() {
        return "INSUFFICIENT_BALANCE";
    }

    @Override
    public Object getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("current_balance", currentBalance);
        return data;
    }
}
