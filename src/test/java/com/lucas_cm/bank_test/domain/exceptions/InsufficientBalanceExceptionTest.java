package com.lucas_cm.bank_test.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InsufficientBalanceException - Testes Unitários")
class InsufficientBalanceExceptionTest {

    @Test
    @DisplayName("Dado um saldo atual, quando criar exceção, então deve retornar mensagem correta")
    void dado_saldo_atual_quando_criar_excecao_entao_deve_retornar_mensagem_correta() {
        // Given - Dado um saldo atual
        BigDecimal currentBalance = new BigDecimal("100.50");
        InsufficientBalanceException exception = new InsufficientBalanceException(currentBalance);

        // When - Quando obter a mensagem
        String message = exception.getMessage();

        // Then - Então deve retornar mensagem correta
        assertThat(message).isNotNull();
        assertThat(message).contains("Saldo insuficiente para realizar esta operação");
        assertThat(message).contains("100.50");
    }

    @Test
    @DisplayName("Dado um saldo atual, quando criar exceção, então deve retornar código de erro correto")
    void dado_saldo_atual_quando_criar_excecao_entao_deve_retornar_codigo_erro_correto() {
        // Given - Dado um saldo atual
        BigDecimal currentBalance = new BigDecimal("50.00");
        InsufficientBalanceException exception = new InsufficientBalanceException(currentBalance);

        // When - Quando obter o código de erro
        String errorCode = exception.getErrorCode();

        // Then - Então deve retornar código de erro correto
        assertThat(errorCode).isNotNull();
        assertThat(errorCode).isEqualTo("INSUFFICIENT_BALANCE");
    }

    @Test
    @DisplayName("Dado um saldo atual, quando criar exceção, então deve retornar dados com saldo atual")
    void dado_saldo_atual_quando_criar_excecao_entao_deve_retornar_dados_com_saldo() {
        // Given - Dado um saldo atual
        BigDecimal currentBalance = new BigDecimal("200.75");
        InsufficientBalanceException exception = new InsufficientBalanceException(currentBalance);

        // When - Quando obter os dados
        Object data = exception.getData();

        // Then - Então deve retornar dados com saldo atual
        assertThat(data).isNotNull();
        assertThat(data).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) data;
        assertThat(dataMap).containsKey("current_balance");
        assertThat(dataMap.get("current_balance")).isEqualTo(currentBalance);
    }

    @Test
    @DisplayName("Dado um saldo zero, quando criar exceção, então deve retornar mensagem com saldo zero")
    void dado_saldo_zero_quando_criar_excecao_entao_deve_retornar_mensagem_com_saldo_zero() {
        // Given - Dado um saldo zero
        BigDecimal currentBalance = BigDecimal.ZERO;
        InsufficientBalanceException exception = new InsufficientBalanceException(currentBalance);

        // When - Quando obter a mensagem
        String message = exception.getMessage();

        // Then - Então deve retornar mensagem com saldo zero
        assertThat(message).isNotNull();
        assertThat(message).contains("0");
    }

    @Test
    @DisplayName("Dado um saldo negativo, quando criar exceção, então deve retornar mensagem com saldo negativo")
    void dado_saldo_negativo_quando_criar_excecao_entao_deve_retornar_mensagem_com_saldo_negativo() {
        // Given - Dado um saldo negativo (caso extremo)
        BigDecimal currentBalance = new BigDecimal("-10.00");
        InsufficientBalanceException exception = new InsufficientBalanceException(currentBalance);

        // When - Quando obter a mensagem
        String message = exception.getMessage();

        // Then - Então deve retornar mensagem com saldo negativo
        assertThat(message).isNotNull();
        assertThat(message).contains("-10.00");
    }
}

