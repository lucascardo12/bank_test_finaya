package com.lucas_cm.bank_test.configuration.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends RuntimeException {
    private String errorCode;
    private String message;
}
