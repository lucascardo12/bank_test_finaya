package com.lucas_cm.bank_test.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TransactionStatusEnum - Testes Unitários")
class TransactionStatusEnumTest {

    @Test
    @DisplayName("Dado o enum TransactionStatusEnum, quando verificar valores, então deve conter PENDING, CONFIRMED e REJECTED")
    void dado_enum_quando_verificar_valores_entao_deve_conter_todos_valores() {
        // Given/When - Dado/Quando verificar os valores do enum
        TransactionStatusEnum[] values = TransactionStatusEnum.values();

        // Then - Então deve conter todos os valores esperados
        assertThat(values).hasSize(3);
        assertThat(values).contains(
                TransactionStatusEnum.PENDING,
                TransactionStatusEnum.CONFIRMED,
                TransactionStatusEnum.REJECTED
        );
    }

    @Test
    @DisplayName("Dado a string 'PENDING' em maiúsculas, quando converter para enum, então deve retornar PENDING")
    void dado_string_pending_maiusculas_quando_converter_entao_deve_retornar_pending() {
        // Given - Dado a string 'PENDING' em maiúsculas
        String eventType = "PENDING";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar PENDING
        assertThat(result).isEqualTo(TransactionStatusEnum.PENDING);
    }

    @Test
    @DisplayName("Dado a string 'pending' em minúsculas, quando converter para enum, então deve retornar PENDING")
    void dado_string_pending_minusculas_quando_converter_entao_deve_retornar_pending() {
        // Given - Dado a string 'pending' em minúsculas
        String eventType = "pending";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar PENDING
        assertThat(result).isEqualTo(TransactionStatusEnum.PENDING);
    }

    @Test
    @DisplayName("Dado a string 'Pending' com primeira letra maiúscula, quando converter para enum, então deve retornar PENDING")
    void dado_string_pending_misturado_quando_converter_entao_deve_retornar_pending() {
        // Given - Dado a string 'Pending' com primeira letra maiúscula
        String eventType = "Pending";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar PENDING
        assertThat(result).isEqualTo(TransactionStatusEnum.PENDING);
    }

    @Test
    @DisplayName("Dado a string 'CONFIRMED' em maiúsculas, quando converter para enum, então deve retornar CONFIRMED")
    void dado_string_confirmed_maiusculas_quando_converter_entao_deve_retornar_confirmed() {
        // Given - Dado a string 'CONFIRMED' em maiúsculas
        String eventType = "CONFIRMED";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar CONFIRMED
        assertThat(result).isEqualTo(TransactionStatusEnum.CONFIRMED);
    }

    @Test
    @DisplayName("Dado a string 'confirmed' em minúsculas, quando converter para enum, então deve retornar CONFIRMED")
    void dado_string_confirmed_minusculas_quando_converter_entao_deve_retornar_confirmed() {
        // Given - Dado a string 'confirmed' em minúsculas
        String eventType = "confirmed";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar CONFIRMED
        assertThat(result).isEqualTo(TransactionStatusEnum.CONFIRMED);
    }

    @Test
    @DisplayName("Dado a string 'Confirmed' com primeira letra maiúscula, quando converter para enum, então deve retornar CONFIRMED")
    void dado_string_confirmed_misturado_quando_converter_entao_deve_retornar_confirmed() {
        // Given - Dado a string 'Confirmed' com primeira letra maiúscula
        String eventType = "Confirmed";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar CONFIRMED
        assertThat(result).isEqualTo(TransactionStatusEnum.CONFIRMED);
    }

    @Test
    @DisplayName("Dado a string 'REJECTED' em maiúsculas, quando converter para enum, então deve retornar REJECTED")
    void dado_string_rejected_maiusculas_quando_converter_entao_deve_retornar_rejected() {
        // Given - Dado a string 'REJECTED' em maiúsculas
        String eventType = "REJECTED";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar REJECTED
        assertThat(result).isEqualTo(TransactionStatusEnum.REJECTED);
    }

    @Test
    @DisplayName("Dado a string 'rejected' em minúsculas, quando converter para enum, então deve retornar REJECTED")
    void dado_string_rejected_minusculas_quando_converter_entao_deve_retornar_rejected() {
        // Given - Dado a string 'rejected' em minúsculas
        String eventType = "rejected";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar REJECTED
        assertThat(result).isEqualTo(TransactionStatusEnum.REJECTED);
    }

    @Test
    @DisplayName("Dado a string 'Rejected' com primeira letra maiúscula, quando converter para enum, então deve retornar REJECTED")
    void dado_string_rejected_misturado_quando_converter_entao_deve_retornar_rejected() {
        // Given - Dado a string 'Rejected' com primeira letra maiúscula
        String eventType = "Rejected";

        // When - Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar REJECTED
        assertThat(result).isEqualTo(TransactionStatusEnum.REJECTED);
    }

    @Test
    @DisplayName("Dado uma string inválida, quando converter para enum, então deve lançar RuntimeException")
    void dado_string_invalida_quando_converter_entao_deve_lancar_runtime_exception() {
        // Given - Dado uma string inválida
        String eventType = "INVALID_STATUS";

        // When/Then - Quando converter para enum, então deve lançar RuntimeException
        assertThatThrownBy(() -> TransactionStatusEnum.fromString(eventType))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Dado uma string vazia, quando converter para enum, então deve lançar RuntimeException")
    void dado_string_vazia_quando_converter_entao_deve_lancar_runtime_exception() {
        // Given - Dado uma string vazia
        String eventType = "";

        // When/Then - Quando converter para enum, então deve lançar RuntimeException
        assertThatThrownBy(() -> TransactionStatusEnum.fromString(eventType))
                .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "pending", "Pending", "PeNdInG"})
    @DisplayName("Dado diferentes variações de case para PENDING, quando converter para enum, então deve retornar PENDING")
    void dado_variacoes_case_pending_quando_converter_entao_deve_retornar_pending(String eventType) {
        // Given/When - Dado/Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar PENDING
        assertThat(result).isEqualTo(TransactionStatusEnum.PENDING);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONFIRMED", "confirmed", "Confirmed", "CoNfIrMeD"})
    @DisplayName("Dado diferentes variações de case para CONFIRMED, quando converter para enum, então deve retornar CONFIRMED")
    void dado_variacoes_case_confirmed_quando_converter_entao_deve_retornar_confirmed(String eventType) {
        // Given/When - Dado/Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar CONFIRMED
        assertThat(result).isEqualTo(TransactionStatusEnum.CONFIRMED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"REJECTED", "rejected", "Rejected", "ReJeCtEd"})
    @DisplayName("Dado diferentes variações de case para REJECTED, quando converter para enum, então deve retornar REJECTED")
    void dado_variacoes_case_rejected_quando_converter_entao_deve_retornar_rejected(String eventType) {
        // Given/When - Dado/Quando converter para enum
        TransactionStatusEnum result = TransactionStatusEnum.fromString(eventType);

        // Then - Então deve retornar REJECTED
        assertThat(result).isEqualTo(TransactionStatusEnum.REJECTED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "UNKNOWN", "ERROR", "NULL", "123"})
    @DisplayName("Dado diferentes strings inválidas, quando converter para enum, então deve lançar RuntimeException")
    void dado_strings_invalidas_quando_converter_entao_deve_lancar_runtime_exception(String eventType) {
        // When/Then - Quando converter para enum, então deve lançar RuntimeException
        assertThatThrownBy(() -> TransactionStatusEnum.fromString(eventType))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Dado o valor PENDING, quando obter nome, então deve retornar 'PENDING'")
    void dado_valor_pending_quando_obter_nome_entao_deve_retornar_pending() {
        // Given - Dado o valor PENDING
        TransactionStatusEnum status = TransactionStatusEnum.PENDING;

        // When - Quando obter o nome
        String name = status.name();

        // Then - Então deve retornar 'PENDING'
        assertThat(name).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Dado o valor CONFIRMED, quando obter nome, então deve retornar 'CONFIRMED'")
    void dado_valor_confirmed_quando_obter_nome_entao_deve_retornar_confirmed() {
        // Given - Dado o valor CONFIRMED
        TransactionStatusEnum status = TransactionStatusEnum.CONFIRMED;

        // When - Quando obter o nome
        String name = status.name();

        // Then - Então deve retornar 'CONFIRMED'
        assertThat(name).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Dado o valor REJECTED, quando obter nome, então deve retornar 'REJECTED'")
    void dado_valor_rejected_quando_obter_nome_entao_deve_retornar_rejected() {
        // Given - Dado o valor REJECTED
        TransactionStatusEnum status = TransactionStatusEnum.REJECTED;

        // When - Quando obter o nome
        String name = status.name();

        // Then - Então deve retornar 'REJECTED'
        assertThat(name).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("Dado uma string 'PENDING', quando usar valueOf, então deve retornar PENDING")
    void dado_string_pending_quando_usar_valueof_entao_deve_retornar_pending() {
        // Given - Dado uma string 'PENDING'
        String statusName = "PENDING";

        // When - Quando usar valueOf
        TransactionStatusEnum result = TransactionStatusEnum.valueOf(statusName);

        // Then - Então deve retornar PENDING
        assertThat(result).isEqualTo(TransactionStatusEnum.PENDING);
    }

    @Test
    @DisplayName("Dado uma string 'CONFIRMED', quando usar valueOf, então deve retornar CONFIRMED")
    void dado_string_confirmed_quando_usar_valueof_entao_deve_retornar_confirmed() {
        // Given - Dado uma string 'CONFIRMED'
        String statusName = "CONFIRMED";

        // When - Quando usar valueOf
        TransactionStatusEnum result = TransactionStatusEnum.valueOf(statusName);

        // Then - Então deve retornar CONFIRMED
        assertThat(result).isEqualTo(TransactionStatusEnum.CONFIRMED);
    }

    @Test
    @DisplayName("Dado uma string 'REJECTED', quando usar valueOf, então deve retornar REJECTED")
    void dado_string_rejected_quando_usar_valueof_entao_deve_retornar_rejected() {
        // Given - Dado uma string 'REJECTED'
        String statusName = "REJECTED";

        // When - Quando usar valueOf
        TransactionStatusEnum result = TransactionStatusEnum.valueOf(statusName);

        // Then - Então deve retornar REJECTED
        assertThat(result).isEqualTo(TransactionStatusEnum.REJECTED);
    }
}

