package com.lucas_cm.bank_test.domain.exceptions;

import com.lucas_cm.bank_test.configuration.exception.BusinessException;

public class WalletNotFoundException extends BusinessException {
    @Override
    public String getErrorCode() {
        return "WALLET_NOT_FOUND";
    }
}
