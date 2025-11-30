package com.lucas_cm.bank_test.domain.exceptions;

import com.lucas_cm.bank_test.configuration.exception.BusinessException;


public class UserAlreadyHasWalletException extends BusinessException {
    @Override
    public String getErrorCode() {
        return "USER_WALLET_ALREADY_EXISTS";
    }
}
