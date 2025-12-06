package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.*;
import com.lucas_cm.bank_test.domain.exceptions.InsufficientBalanceException;
import com.lucas_cm.bank_test.domain.exceptions.PixTransferNotFoundException;
import com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException;
import com.lucas_cm.bank_test.domain.repositories.EventPixRepository;
import com.lucas_cm.bank_test.domain.repositories.TransactionRepository;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferRequest;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferResponse;
import com.lucas_cm.bank_test.infrastructure.dtos.PixWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PixService - Testes Unitários")
class PixServiceTest {

    @Mock
    private EventPixRepository eventPixRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private PixService pixService;

    private String idempotencyKey;
    private String fromWalletId;
    private String toWalletId;
    private String toPixKey;
    private BigDecimal transferAmount;
    private WalletEntity fromWallet;
    private WalletEntity toWallet;
    private PixTransferRequest transferRequest;
    private BigDecimal fromWalletBalance;

    @BeforeEach
    void setUp() {
        idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
        fromWalletId = "wallet-from-123";
        toWalletId = "wallet-to-456";
        toPixKey = "pix-key-789";
        transferAmount = new BigDecimal("100.50");
        fromWalletBalance = new BigDecimal("1000.00");

        fromWallet = WalletEntity.builder()
                .id(fromWalletId)
                .userId("user-from")
                .currentBalance(fromWalletBalance)
                .pixKey("pix-key-from")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        toWallet = WalletEntity.builder()
                .id(toWalletId)
                .userId("user-to")
                .currentBalance(new BigDecimal("500.00"))
                .pixKey(toPixKey)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transferRequest = new PixTransferRequest(
                fromWalletId,
                toPixKey,
                transferAmount
        );
    }

    @Test
    @DisplayName("Dado uma requisição de transferência PIX válida, quando transferir, então deve criar transações de débito e crédito")
    void dado_requisicao_valida_quando_transferir_entao_deve_criar_transacoes() {
        // Given - Dado uma requisição válida
        when(transactionRepository.findByEndToEndId("OUT" + idempotencyKey))
                .thenReturn(Optional.empty());
        when(walletRepository.findByIdWithLock(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByPixKeyWithLock(toPixKey)).thenReturn(Optional.of(toWallet));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - Quando transferir
        PixTransferResponse response = pixService.transfer(idempotencyKey, transferRequest);

        // Then - Então deve criar transações e retornar resposta
        assertThat(response).isNotNull();
        assertThat(response.endToEndId()).isEqualTo(idempotencyKey);
        assertThat(response.status()).isEqualTo(TransactionStatusEnum.PENDING);

        // Verificar criação de transação de débito
        ArgumentCaptor<TransactionEntity> debitCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionRepository, times(2)).save(debitCaptor.capture());

        TransactionEntity debit = debitCaptor.getAllValues().get(0);
        assertThat(debit.getWalletId()).isEqualTo(fromWalletId);
        assertThat(debit.getEndToEndId()).isEqualTo("OUT" + idempotencyKey);
        assertThat(debit.getAmount()).isEqualTo(transferAmount.negate());
        assertThat(debit.getType()).isEqualTo(TransactionTypeEnum.PIX_TRANSFER_OUT);
        assertThat(debit.getStatus()).isEqualTo(TransactionStatusEnum.PENDING);
        assertThat(debit.getPixKey()).isEqualTo(toPixKey);

        // Verificar criação de transação de crédito
        TransactionEntity credit = debitCaptor.getAllValues().get(1);
        assertThat(credit.getWalletId()).isEqualTo(toWalletId);
        assertThat(credit.getEndToEndId()).isEqualTo("IN" + idempotencyKey);
        assertThat(credit.getAmount()).isEqualTo(transferAmount);
        assertThat(credit.getType()).isEqualTo(TransactionTypeEnum.PIX_TRANSFER_IN);
        assertThat(credit.getStatus()).isEqualTo(TransactionStatusEnum.PENDING);
        assertThat(credit.getPixKey()).isEqualTo(toPixKey);
    }

    @Test
    @DisplayName("Dado uma requisição de transferência PIX com idempotência, quando transferir, então deve retornar transação existente")
    void dado_requisicao_com_idempotencia_quando_transferir_entao_deve_retornar_existente() {
        // Given - Dado que já existe uma transação com o mesmo idempotencyKey
        TransactionEntity existingTransaction = TransactionEntity.builder()
                .id(1L)
                .walletId(fromWalletId)
                .endToEndId("OUT" + idempotencyKey)
                .amount(transferAmount.negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByEndToEndId("OUT" + idempotencyKey))
                .thenReturn(Optional.of(existingTransaction));

        // When - Quando transferir
        PixTransferResponse response = pixService.transfer(idempotencyKey, transferRequest);

        // Then - Então deve retornar a transação existente
        assertThat(response).isNotNull();
        assertThat(response.endToEndId()).isEqualTo(idempotencyKey);
        assertThat(response.status()).isEqualTo(TransactionStatusEnum.CONFIRMED);

        verify(transactionRepository).findByEndToEndId("OUT" + idempotencyKey);
        verify(walletRepository, never()).findByIdWithLock(any());
        verify(walletRepository, never()).findByPixKeyWithLock(any());
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Dado uma transferência PIX com saldo insuficiente, quando transferir, então deve lançar exceção InsufficientBalanceException")
    void dado_transferencia_com_saldo_insuficiente_quando_transferir_entao_deve_lancar_excecao() {
        // Given - Dado que a carteira de origem não tem saldo suficiente
        fromWallet.setCurrentBalance(new BigDecimal("50.00"));
        when(transactionRepository.findByEndToEndId("OUT" + idempotencyKey))
                .thenReturn(Optional.empty());
        when(walletRepository.findByIdWithLock(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByPixKeyWithLock(toPixKey)).thenReturn(Optional.of(toWallet));

        // When/Then - Quando transferir, então deve lançar exceção
        assertThatThrownBy(() -> pixService.transfer(idempotencyKey, transferRequest))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(walletRepository).findByIdWithLock(fromWalletId);
        verify(walletRepository).findByPixKeyWithLock(toPixKey);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Dado uma transferência PIX com carteira de origem inexistente, quando transferir, então deve lançar exceção WalletNotFoundException")
    void dado_carteira_origem_inexistente_quando_transferir_entao_deve_lancar_excecao() {
        // Given - Dado que a carteira de origem não existe
        when(transactionRepository.findByEndToEndId("OUT" + idempotencyKey))
                .thenReturn(Optional.empty());
        when(walletRepository.findByIdWithLock(fromWalletId))
                .thenReturn(Optional.empty());

        // When/Then - Quando transferir, então deve lançar exceção
        assertThatThrownBy(() -> pixService.transfer(idempotencyKey, transferRequest))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findByIdWithLock(fromWalletId);
        verify(walletRepository, never()).findByPixKeyWithLock(any());
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Dado uma transferência PIX com chave PIX inexistente, quando transferir, então deve lançar exceção WalletNotFoundException")
    void dado_chave_pix_inexistente_quando_transferir_entao_deve_lancar_excecao() {
        // Given - Dado que a chave PIX de destino não existe
        when(transactionRepository.findByEndToEndId("OUT" + idempotencyKey))
                .thenReturn(Optional.empty());
        when(walletRepository.findByIdWithLock(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByPixKeyWithLock(toPixKey))
                .thenReturn(Optional.empty());

        // When/Then - Quando transferir, então deve lançar exceção
        assertThatThrownBy(() -> pixService.transfer(idempotencyKey, transferRequest))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findByIdWithLock(fromWalletId);
        verify(walletRepository).findByPixKeyWithLock(toPixKey);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Dado um webhook CONFIRMED válido, quando processar webhook, então deve atualizar status e saldos")
    void dado_webhook_confirmed_valido_quando_processar_entao_deve_atualizar_status_e_saldos() {
        // Given - Dado um webhook CONFIRMED
        String endToEndId = "E2E123456789";
        String eventId = "event-123";
        String occurredAt = "2025-01-01T10:00:00Z";

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                endToEndId,
                eventId,
                "CONFIRMED",
                occurredAt
        );

        TransactionEntity debit = TransactionEntity.builder()
                .id(1L)
                .walletId(fromWalletId)
                .endToEndId("OUT" + endToEndId)
                .amount(transferAmount.negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionEntity credit = TransactionEntity.builder()
                .id(2L)
                .walletId(toWalletId)
                .endToEndId("IN" + endToEndId)
                .amount(transferAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .status(TransactionStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventPixRepository.existsByEventId(eventId)).thenReturn(false);
        when(transactionRepository.findByEndToEndId("OUT" + endToEndId))
                .thenReturn(Optional.of(debit));
        when(transactionRepository.findByEndToEndId("IN" + endToEndId))
                .thenReturn(Optional.of(credit));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPixRepository.save(any(EventPixEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.findByIdWithLock(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByIdWithLock(toWalletId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(WalletEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - Quando processar o webhook
        pixService.processWebhook(webhookRequest);

        // Then - Então deve atualizar status e saldos
        assertThat(debit.getStatus()).isEqualTo(TransactionStatusEnum.CONFIRMED);
        assertThat(credit.getStatus()).isEqualTo(TransactionStatusEnum.CONFIRMED);

        verify(transactionRepository).save(debit);
        verify(transactionRepository).save(credit);
        verify(eventPixRepository).save(any(EventPixEntity.class));
        verify(walletRepository, times(2)).findByIdWithLock(any());
        verify(walletRepository, times(2)).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um webhook REJECTED válido, quando processar webhook, então deve atualizar status mas não atualizar saldos")
    void dado_webhook_rejected_valido_quando_processar_entao_deve_atualizar_status_sem_saldos() {
        // Given - Dado um webhook REJECTED
        String endToEndId = "E2E123456789";
        String eventId = "event-456";
        String occurredAt = "2025-01-01T10:00:00Z";

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                endToEndId,
                eventId,
                "REJECTED",
                occurredAt
        );

        TransactionEntity debit = TransactionEntity.builder()
                .id(1L)
                .walletId(fromWalletId)
                .endToEndId("OUT" + endToEndId)
                .amount(transferAmount.negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionEntity credit = TransactionEntity.builder()
                .id(2L)
                .walletId(toWalletId)
                .endToEndId("IN" + endToEndId)
                .amount(transferAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .status(TransactionStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventPixRepository.existsByEventId(eventId)).thenReturn(false);
        when(transactionRepository.findByEndToEndId("OUT" + endToEndId))
                .thenReturn(Optional.of(debit));
        when(transactionRepository.findByEndToEndId("IN" + endToEndId))
                .thenReturn(Optional.of(credit));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPixRepository.save(any(EventPixEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - Quando processar o webhook
        pixService.processWebhook(webhookRequest);

        // Then - Então deve atualizar status mas não atualizar saldos
        assertThat(debit.getStatus()).isEqualTo(TransactionStatusEnum.REJECTED);
        assertThat(credit.getStatus()).isEqualTo(TransactionStatusEnum.REJECTED);

        verify(transactionRepository).save(debit);
        verify(transactionRepository).save(credit);
        verify(eventPixRepository).save(any(EventPixEntity.class));
        verify(walletRepository, never()).findByIdWithLock(any());
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um webhook com eventId já processado, quando processar webhook, então deve retornar sem processar")
    void dado_webhook_com_eventid_ja_processado_quando_processar_entao_deve_retornar_sem_processar() {
        // Given - Dado que o eventId já foi processado
        String eventId = "event-789";
        EventPixEntity existingEvent = EventPixEntity.builder()
                .id(1L)
                .eventId(eventId)
                .endToEndId("E2E123")
                .eventType(TransactionStatusEnum.CONFIRMED)
                .occurredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                "E2E123",
                eventId,
                "CONFIRMED",
                "2025-01-01T10:00:00Z"
        );

        when(eventPixRepository.existsByEventId(eventId))
                .thenReturn(true);

        // When - Quando processar o webhook
        pixService.processWebhook(webhookRequest);

        // Then - Então deve retornar sem processar
        verify(eventPixRepository).existsByEventId(eventId);
        verify(transactionRepository, never()).findByEndToEndId(any());
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
        verify(eventPixRepository, never()).save(any(EventPixEntity.class));
    }

    @Test
    @DisplayName("Dado um webhook com transações não encontradas, quando processar webhook, então deve lançar exceção PixTransferNotFoundException")
    void dado_webhook_com_transacoes_nao_encontradas_quando_processar_entao_deve_lancar_excecao() {
        // Given - Dado que as transações não existem
        String endToEndId = "E2E123456789";
        String eventId = "event-999";

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                endToEndId,
                eventId,
                "CONFIRMED",
                "2025-01-01T10:00:00Z"
        );

        when(eventPixRepository.existsByEventId(eventId)).thenReturn(false);
        when(transactionRepository.findByEndToEndId("OUT" + endToEndId))
                .thenReturn(Optional.empty());

        // When/Then - Quando processar o webhook, então deve lançar exceção
        assertThatThrownBy(() -> pixService.processWebhook(webhookRequest))
                .isInstanceOf(PixTransferNotFoundException.class);

        verify(eventPixRepository).existsByEventId(eventId);
        verify(transactionRepository).findByEndToEndId("OUT" + endToEndId);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Dado um webhook com transações já processadas, quando processar webhook, então deve retornar sem atualizar")
    void dado_webhook_com_transacoes_ja_processadas_quando_processar_entao_deve_retornar_sem_atualizar() {
        // Given - Dado que as transações já foram processadas (não são PENDING)
        String endToEndId = "E2E123456789";
        String eventId = "event-111";

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                endToEndId,
                eventId,
                "CONFIRMED",
                "2025-01-01T10:00:00Z"
        );

        TransactionEntity debit = TransactionEntity.builder()
                .id(1L)
                .walletId(fromWalletId)
                .endToEndId("OUT" + endToEndId)
                .amount(transferAmount.negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.CONFIRMED) // Já processada
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionEntity credit = TransactionEntity.builder()
                .id(2L)
                .walletId(toWalletId)
                .endToEndId("IN" + endToEndId)
                .amount(transferAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .status(TransactionStatusEnum.CONFIRMED) // Já processada
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventPixRepository.existsByEventId(eventId)).thenReturn(false);
        when(transactionRepository.findByEndToEndId("OUT" + endToEndId))
                .thenReturn(Optional.of(debit));
        when(transactionRepository.findByEndToEndId("IN" + endToEndId))
                .thenReturn(Optional.of(credit));

        // When - Quando processar o webhook
        pixService.processWebhook(webhookRequest);

        // Then - Então deve retornar sem atualizar
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
        verify(eventPixRepository, never()).save(any(EventPixEntity.class));
        verify(walletRepository, never()).findByIdWithLock(any());
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um webhook PENDING válido, quando processar webhook, então deve atualizar status mas não atualizar saldos")
    void dado_webhook_pending_valido_quando_processar_entao_deve_atualizar_status_sem_saldos() {
        // Given - Dado um webhook PENDING
        String endToEndId = "E2E123456789";
        String eventId = "event-222";
        String occurredAt = "2025-01-01T10:00:00Z";

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                endToEndId,
                eventId,
                "PENDING",
                occurredAt
        );

        TransactionEntity debit = TransactionEntity.builder()
                .id(1L)
                .walletId(fromWalletId)
                .endToEndId("OUT" + endToEndId)
                .amount(transferAmount.negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionEntity credit = TransactionEntity.builder()
                .id(2L)
                .walletId(toWalletId)
                .endToEndId("IN" + endToEndId)
                .amount(transferAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .status(TransactionStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventPixRepository.existsByEventId(eventId)).thenReturn(false);
        when(transactionRepository.findByEndToEndId("OUT" + endToEndId))
                .thenReturn(Optional.of(debit));
        when(transactionRepository.findByEndToEndId("IN" + endToEndId))
                .thenReturn(Optional.of(credit));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPixRepository.save(any(EventPixEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - Quando processar o webhook
        pixService.processWebhook(webhookRequest);

        // Then - Então deve atualizar status mas não atualizar saldos
        assertThat(debit.getStatus()).isEqualTo(TransactionStatusEnum.PENDING);
        assertThat(credit.getStatus()).isEqualTo(TransactionStatusEnum.PENDING);

        verify(transactionRepository).save(debit);
        verify(transactionRepository).save(credit);
        verify(eventPixRepository).save(any(EventPixEntity.class));
        verify(walletRepository, never()).findByIdWithLock(any());
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado uma transferência PIX com saldo exato, quando transferir, então deve permitir a transferência")
    void dado_transferencia_com_saldo_exato_quando_transferir_entao_deve_permitir() {
        // Given - Dado que o saldo é exatamente igual ao valor da transferência
        fromWallet.setCurrentBalance(transferAmount);
        when(transactionRepository.findByEndToEndId("OUT" + idempotencyKey))
                .thenReturn(Optional.empty());
        when(walletRepository.findByIdWithLock(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByPixKeyWithLock(toPixKey)).thenReturn(Optional.of(toWallet));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - Quando transferir
        PixTransferResponse response = pixService.transfer(idempotencyKey, transferRequest);

        // Then - Então deve permitir a transferência
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(TransactionStatusEnum.PENDING);
        verify(transactionRepository, times(2)).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Dado um webhook REJECTED tentando sobrescrever CONFIRMED, quando processar webhook, então deve ignorar")
    void dado_webhook_rejected_sobrescrevendo_confirmed_quando_processar_entao_deve_ignorar() {
        // Given - Dado que a transação já está CONFIRMED e recebe REJECTED
        String endToEndId = "E2E123456789";
        String eventId = "event-reject-after-confirm";

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                endToEndId,
                eventId,
                "REJECTED",
                "2025-01-01T10:00:00Z"
        );

        TransactionEntity debit = TransactionEntity.builder()
                .id(1L)
                .walletId(fromWalletId)
                .endToEndId("OUT" + endToEndId)
                .amount(transferAmount.negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.CONFIRMED) // Já confirmada
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionEntity credit = TransactionEntity.builder()
                .id(2L)
                .walletId(toWalletId)
                .endToEndId("IN" + endToEndId)
                .amount(transferAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .status(TransactionStatusEnum.CONFIRMED) // Já confirmada
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventPixRepository.existsByEventId(eventId)).thenReturn(false);
        when(transactionRepository.findByEndToEndId("OUT" + endToEndId))
                .thenReturn(Optional.of(debit));
        when(transactionRepository.findByEndToEndId("IN" + endToEndId))
                .thenReturn(Optional.of(credit));

        // When - Quando processar o webhook
        pixService.processWebhook(webhookRequest);

        // Then - Então deve ignorar e não atualizar
        assertThat(debit.getStatus()).isEqualTo(TransactionStatusEnum.CONFIRMED);
        assertThat(credit.getStatus()).isEqualTo(TransactionStatusEnum.CONFIRMED);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
        verify(eventPixRepository, never()).save(any(EventPixEntity.class));
        verify(walletRepository, never()).findByIdWithLock(any());
    }

    @Test
    @DisplayName("Dado um webhook CONFIRMED tentando sobrescrever REJECTED, quando processar webhook, então deve ignorar")
    void dado_webhook_confirmed_sobrescrevendo_rejected_quando_processar_entao_deve_ignorar() {
        // Given - Dado que a transação já está REJECTED e recebe CONFIRMED
        String endToEndId = "E2E123456789";
        String eventId = "event-confirm-after-reject";

        PixWebhookRequest webhookRequest = new PixWebhookRequest(
                endToEndId,
                eventId,
                "CONFIRMED",
                "2025-01-01T10:00:00Z"
        );

        TransactionEntity debit = TransactionEntity.builder()
                .id(1L)
                .walletId(fromWalletId)
                .endToEndId("OUT" + endToEndId)
                .amount(transferAmount.negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.REJECTED) // Já rejeitada
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionEntity credit = TransactionEntity.builder()
                .id(2L)
                .walletId(toWalletId)
                .endToEndId("IN" + endToEndId)
                .amount(transferAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .status(TransactionStatusEnum.REJECTED) // Já rejeitada
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(eventPixRepository.existsByEventId(eventId)).thenReturn(false);
        when(transactionRepository.findByEndToEndId("OUT" + endToEndId))
                .thenReturn(Optional.of(debit));
        when(transactionRepository.findByEndToEndId("IN" + endToEndId))
                .thenReturn(Optional.of(credit));

        // When - Quando processar o webhook
        pixService.processWebhook(webhookRequest);

        // Then - Então deve ignorar e não atualizar
        assertThat(debit.getStatus()).isEqualTo(TransactionStatusEnum.REJECTED);
        assertThat(credit.getStatus()).isEqualTo(TransactionStatusEnum.REJECTED);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
        verify(eventPixRepository, never()).save(any(EventPixEntity.class));
        verify(walletRepository, never()).findByIdWithLock(any());
    }
}

