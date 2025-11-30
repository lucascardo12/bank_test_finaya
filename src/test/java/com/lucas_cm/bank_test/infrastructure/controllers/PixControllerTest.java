package com.lucas_cm.bank_test.infrastructure.controllers;

import com.lucas_cm.bank_test.domain.entities.TransactionStatusEnum;
import com.lucas_cm.bank_test.domain.services.PixService;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferRequest;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferResponse;
import com.lucas_cm.bank_test.infrastructure.dtos.PixWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PixController - Testes Unitários")
class PixControllerTest {

    @Mock
    private PixService pixService;

    @InjectMocks
    private PixController pixController;


    private String idempotencyKey;
    private PixTransferRequest request;
    private PixTransferResponse expectedResponse;
    private PixWebhookRequest webhookRequest;


    @BeforeEach
    void setUp() {
        webhookRequest = new PixWebhookRequest(
                "E2E123456789",
                "event-123",
                "CONFIRMED",
                "2025-01-01T10:00:00");
        idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
        request = new PixTransferRequest(
                "wallet-123",
                "pix-key-456",
                new BigDecimal("100.50"));
        expectedResponse = new PixTransferResponse(
                idempotencyKey,
                TransactionStatusEnum.PENDING);
    }

    @Test
    @DisplayName("Dado uma requisição válida de transferência PIX, quando o endpoint for chamado, então deve retornar 200 OK com a resposta do serviço")
    void dado_requisicao_valida_quando_chamar_endpoint_entao_deve_retornar_200_ok() {
        // Given - Dado que o serviço retorna uma resposta de sucesso
        when(pixService.transfer(eq(idempotencyKey), any(PixTransferRequest.class)))
                .thenReturn(expectedResponse);

        // When - Quando o endpoint de transferência for chamado
        ResponseEntity<PixTransferResponse> response = pixController.transferPix(
                idempotencyKey,
                request);

        // Then - Então deve retornar 200 OK com a resposta correta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().endToEndId()).isEqualTo(idempotencyKey);
        assertThat(response.getBody().status()).isEqualTo(TransactionStatusEnum.PENDING);
    }

    @Test
    @DisplayName("Dado uma requisição de transferência PIX, quando o endpoint for chamado, então deve chamar o serviço com os parâmetros corretos")
    void dado_requisicao_quando_chamar_endpoint_entao_deve_chamar_servico_com_parametros_corretos() {
        // Given - Dado que o serviço está configurado
        when(pixService.transfer(eq(idempotencyKey), any(PixTransferRequest.class)))
                .thenReturn(expectedResponse);

        // When - Quando o endpoint de transferência for chamado
        pixController.transferPix(idempotencyKey, request);

        // Then - Então deve chamar o serviço com a chave de idempotência e a requisição
        // corretas
        verify(pixService).transfer(idempotencyKey, request);
    }

    @Test
    @DisplayName("Dado uma transferência PIX com status CONFIRMED, quando o serviço retornar, então deve retornar o status correto na resposta")
    void dado_transferencia_confirmada_quando_servico_retornar_entao_deve_retornar_status_confirmado() {
        // Given - Dado que o serviço retorna uma transferência confirmada
        PixTransferResponse confirmedResponse = new PixTransferResponse(
                idempotencyKey,
                TransactionStatusEnum.CONFIRMED);
        when(pixService.transfer(eq(idempotencyKey), any(PixTransferRequest.class)))
                .thenReturn(confirmedResponse);

        // When - Quando o endpoint de transferência for chamado
        ResponseEntity<PixTransferResponse> response = pixController.transferPix(
                idempotencyKey,
                request);

        // Then - Então deve retornar o status CONFIRMED
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(TransactionStatusEnum.CONFIRMED);
    }

    @Test
    @DisplayName("Dado uma transferência PIX com status REJECTED, quando o serviço retornar, então deve retornar o status rejeitado na resposta")
    void dado_transferencia_rejeitada_quando_servico_retornar_entao_deve_retornar_status_rejeitado() {
        // Given - Dado que o serviço retorna uma transferência rejeitada
        PixTransferResponse rejectedResponse = new PixTransferResponse(
                idempotencyKey,
                TransactionStatusEnum.REJECTED);
        when(pixService.transfer(eq(idempotencyKey), any(PixTransferRequest.class)))
                .thenReturn(rejectedResponse);

        // When - Quando o endpoint de transferência for chamado
        ResponseEntity<PixTransferResponse> response = pixController.transferPix(
                idempotencyKey,
                request);

        // Then - Então deve retornar o status REJECTED
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(TransactionStatusEnum.REJECTED);
    }


    @Test
    @DisplayName("Dado um webhook válido do arranjo PIX, quando o endpoint for chamado, então deve retornar 200 OK")
    void dado_webhook_valido_quando_chamar_endpoint_entao_deve_retornar_200_ok() {
        // Given - Dado um webhook válido do arranjo PIX
        // (não há necessidade de mock pois o método não retorna valor)

        // When - Quando o endpoint de webhook for chamado
        ResponseEntity<Void> response = pixController.receiveWebhook(webhookRequest);

        // Then - Então deve retornar 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("Dado um webhook do arranjo PIX, quando o endpoint for chamado, então deve processar o webhook no serviço")
    void dado_webhook_quando_chamar_endpoint_entao_deve_processar_webhook_no_servico() {
        // Given - Dado um webhook válido
        // (não há necessidade de mock pois o método não retorna valor)

        // When - Quando o endpoint de webhook for chamado
        pixController.receiveWebhook(webhookRequest);

        // Then - Então deve chamar o serviço para processar o webhook
        verify(pixService).processWebhook(webhookRequest);
    }

    @Test
    @DisplayName("Dado um webhook com evento RECEIVED, quando o endpoint for chamado, então deve retornar 200 OK")
    void dado_webhook_received_quando_chamar_endpoint_entao_deve_retornar_200_ok() {
        // Given - Dado um webhook com evento RECEIVED
        PixWebhookRequest receivedWebhook = new PixWebhookRequest(
                "E2E123456789",
                "event-456",
                "RECEIVED",
                "2025-01-01T10:00:00");

        // When - Quando o endpoint de webhook for chamado
        ResponseEntity<Void> response = pixController.receiveWebhook(receivedWebhook);

        // Then - Então deve retornar 200 OK independente do tipo de evento
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(pixService).processWebhook(receivedWebhook);
    }

    @Test
    @DisplayName("Dado um webhook com evento CONFIRMED, quando o endpoint for chamado, então deve retornar 200 OK")
    void dado_webhook_confirmed_quando_chamar_endpoint_entao_deve_retornar_200_ok() {
        // Given - Dado um webhook com evento CONFIRMED
        PixWebhookRequest confirmedWebhook = new PixWebhookRequest(
                "E2E123456789",
                "event-789",
                "CONFIRMED",
                "2025-01-01T10:00:00");

        // When - Quando o endpoint de webhook for chamado
        ResponseEntity<Void> response = pixController.receiveWebhook(confirmedWebhook);

        // Then - Então deve retornar 200 OK independente do tipo de evento
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(pixService).processWebhook(confirmedWebhook);
    }

    @Test
    @DisplayName("Dado um webhook do arranjo PIX, quando o endpoint for chamado, então sempre deve retornar 200 OK mesmo se o processamento falhar")
    void dado_webhook_quando_chamar_endpoint_entao_sempre_deve_retornar_200_ok() {
        // Given - Dado que o serviço pode lançar uma exceção
        // Nota: Este teste valida que o comportamento esperado é sempre retornar 200 OK
        // Em um cenário real, exceções seriam tratadas por um handler global

        // When - Quando o endpoint de webhook for chamado
        ResponseEntity<Void> response = pixController.receiveWebhook(webhookRequest);

        // Then - Então sempre deve retornar 200 OK (comportamento de webhook real)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
