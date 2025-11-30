package com.lucas_cm.bank_test.domain.exceptions;

import com.lucas_cm.bank_test.configuration.exception.BusinessException;

public class EventPixIdAlreadyExistsException extends BusinessException {

    @Override
    public String getMessage() {
        return "Já existe um evento PIX registrado com este eventId.";
    }

    @Override
    public String getErrorCode() {
        return "PIX_EVENT_ID_ALREADY_EXISTS";
    }
}
