package com.lucas_cm.bank_test.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventPixIdAlreadyExistsException - Testes Unitários")
class EventPixIdAlreadyExistsExceptionTest {

    @Test
    @DisplayName("Dado uma exceção criada, quando obter mensagem, então deve retornar mensagem correta")
    void dado_excecao_criada_quando_obter_mensagem_entao_deve_retornar_mensagem_correta() {
        // Given - Dado uma exceção criada
        EventPixIdAlreadyExistsException exception = new EventPixIdAlreadyExistsException();

        // When - Quando obter a mensagem
        String message = exception.getMessage();

        // Then - Então deve retornar mensagem correta
        assertThat(message).isNotNull();
        assertThat(message).isEqualTo("Já existe um evento PIX registrado com este eventId.");
    }

    @Test
    @DisplayName("Dado uma exceção criada, quando obter código de erro, então deve retornar código correto")
    void dado_excecao_criada_quando_obter_codigo_erro_entao_deve_retornar_codigo_correto() {
        // Given - Dado uma exceção criada
        EventPixIdAlreadyExistsException exception = new EventPixIdAlreadyExistsException();

        // When - Quando obter o código de erro
        String errorCode = exception.getErrorCode();

        // Then - Então deve retornar código de erro correto
        assertThat(errorCode).isNotNull();
        assertThat(errorCode).isEqualTo("PIX_EVENT_ID_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("Dado uma exceção criada, quando verificar tipo, então deve ser instância de BusinessException")
    void dado_excecao_criada_quando_verificar_tipo_entao_deve_ser_instancia_business_exception() {
        // Given - Dado uma exceção criada
        EventPixIdAlreadyExistsException exception = new EventPixIdAlreadyExistsException();

        // When/Then - Quando verificar o tipo, então deve ser instância de BusinessException
        assertThat(exception).isInstanceOf(com.lucas_cm.bank_test.configuration.exception.BusinessException.class);
    }
}

