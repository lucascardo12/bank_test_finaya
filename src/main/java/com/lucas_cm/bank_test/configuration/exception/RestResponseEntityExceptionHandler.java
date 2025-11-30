package com.lucas_cm.bank_test.configuration.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessError(BusinessException e) {
        log.info("BusinessException", e);
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("error_code", e.getErrorCode());

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalServerError(Exception e) {
        log.error("Exception", e);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Ocorreu um erro no serviço");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundExceptionError(NotFoundException e) {
        log.info("NotFoundException", e);
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("error_code", e.getErrorCode());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}
