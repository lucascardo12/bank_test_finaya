package com.lucas_cm.bank_test.domain.exceptions;

import com.lucas_cm.bank_test.configuration.exception.BusinessException;

public class TransactionEndToEndIdAlreadyExistsException extends BusinessException {
    @Override
    public String getMessage() {
        return "Já existe uma transação registrada com este endToEndId.";
    }

    @Override
    public String getErrorCode() {
        return "TRANSACTION_END_TO_END_ID_ALREADY_EXISTS";
    }
}
