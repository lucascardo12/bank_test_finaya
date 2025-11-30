package com.lucas_cm.bank_test.domain.exceptions;

import com.lucas_cm.bank_test.configuration.exception.BusinessException;

public class PixTransferNotFoundException extends BusinessException {

    @Override
    public String getMessage() {
        return "Transferência PIX não encontrada.";
    }

    @Override
    public String getErrorCode() {
        return "PIX_TRANSFER_NOT_FOUND";
    }
}