package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;
import com.lucas_cm.bank_test.domain.entities.TransactionStatusEnum;
import com.lucas_cm.bank_test.domain.entities.TransactionTypeEnum;
import com.lucas_cm.bank_test.domain.exceptions.TransactionEndToEndIdAlreadyExistsException;
import com.lucas_cm.bank_test.domain.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService - Testes Unitários")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private String walletId;
    private String endToEndId;
    private TransactionEntity transactionEntity;
    private BigDecimal transactionAmount;
    private LocalDateTime transactionDate;

    @BeforeEach
    void setUp() {
        walletId = "wallet-123";
        endToEndId = "E2E123456789";
        transactionAmount = new BigDecimal("100.50");
        transactionDate = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

        transactionEntity = TransactionEntity.builder()
                .id(1L)
                .endToEndId(endToEndId)
                .walletId(walletId)
                .amount(transactionAmount)
                .type(TransactionTypeEnum.DEPOSIT)
                .status(TransactionStatusEnum.CONFIRMED)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();
    }

    @Test
    @DisplayName("Dado uma transação válida com endToEndId único, quando criar transação, então deve salvar e retornar a transação")
    void dado_transacao_valida_com_endtoendid_unico_quando_criar_entao_deve_salvar_e_retornar() {
        // Given - Dado que não existe transação com o mesmo endToEndId
        when(transactionRepository.findByEndToEndId(endToEndId)).thenReturn(Optional.empty());
        when(transactionRepository.save(transactionEntity)).thenReturn(transactionEntity);

        // When - Quando criar a transação
        TransactionEntity result = transactionService.create(transactionEntity);

        // Then - Então deve salvar e retornar a transação
        assertThat(result).isNotNull();
        assertThat(result.getEndToEndId()).isEqualTo(endToEndId);
        assertThat(result.getWalletId()).isEqualTo(walletId);
        assertThat(result.getAmount()).isEqualTo(transactionAmount);
        verify(transactionRepository).findByEndToEndId(endToEndId);
        verify(transactionRepository).save(transactionEntity);
    }

    @Test
    @DisplayName("Dado uma transação com endToEndId já existente, quando criar transação, então deve lançar exceção TransactionEndToEndIdAlreadyExistsException")
    void dado_transacao_com_endtoendid_existente_quando_criar_entao_deve_lancar_excecao() {
        // Given - Dado que já existe uma transação com o mesmo endToEndId
        TransactionEntity existingTransaction = TransactionEntity.builder()
                .id(2L)
                .endToEndId(endToEndId)
                .walletId("wallet-456")
                .amount(new BigDecimal("200.00"))
                .type(TransactionTypeEnum.WITHDRAW)
                .status(TransactionStatusEnum.CONFIRMED)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();

        when(transactionRepository.findByEndToEndId(endToEndId)).thenReturn(Optional.of(existingTransaction));

        // When/Then - Quando criar a transação, então deve lançar exceção
        assertThatThrownBy(() -> transactionService.create(transactionEntity))
                .isInstanceOf(TransactionEndToEndIdAlreadyExistsException.class);

        verify(transactionRepository).findByEndToEndId(endToEndId);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Dado um walletId com transações existentes, quando buscar por walletId, então deve retornar lista de transações")
    void dado_walletid_com_transacoes_quando_buscar_por_walletid_entao_deve_retornar_lista() {
        // Given - Dado que existem transações para o walletId
        TransactionEntity transaction1 = TransactionEntity.builder()
                .id(1L)
                .endToEndId("E2E001")
                .walletId(walletId)
                .amount(new BigDecimal("100.00"))
                .type(TransactionTypeEnum.DEPOSIT)
                .status(TransactionStatusEnum.CONFIRMED)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();

        TransactionEntity transaction2 = TransactionEntity.builder()
                .id(2L)
                .endToEndId("E2E002")
                .walletId(walletId)
                .amount(new BigDecimal("50.00"))
                .type(TransactionTypeEnum.WITHDRAW)
                .status(TransactionStatusEnum.CONFIRMED)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();

        List<TransactionEntity> transactions = Arrays.asList(transaction1, transaction2);
        when(transactionRepository.findByWalletId(walletId)).thenReturn(Optional.of(transactions));

        // When - Quando buscar por walletId
        List<TransactionEntity> result = transactionService.findByWalletId(walletId);

        // Then - Então deve retornar a lista de transações
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(transaction1, transaction2);
        verify(transactionRepository).findByWalletId(walletId);
    }

    @Test
    @DisplayName("Dado um walletId sem transações, quando buscar por walletId, então deve retornar lista vazia")
    void dado_walletid_sem_transacoes_quando_buscar_por_walletid_entao_deve_retornar_lista_vazia() {
        // Given - Dado que não existem transações para o walletId
        when(transactionRepository.findByWalletId(walletId)).thenReturn(Optional.empty());

        // When - Quando buscar por walletId
        List<TransactionEntity> result = transactionService.findByWalletId(walletId);

        // Then - Então deve retornar lista vazia
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(transactionRepository).findByWalletId(walletId);
    }

    @Test
    @DisplayName("Dado um walletId com transações confirmadas, quando calcular saldo por data, então deve retornar o valor correto")
    void dado_walletid_com_transacoes_confirmadas_quando_calcular_saldo_por_data_entao_deve_retornar_valor_correto() {
        // Given - Dado um saldo calculado
        BigDecimal expectedBalance = new BigDecimal("500.75");
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

        when(transactionRepository.amountByWalletIdAndDate(walletId, dateTime))
                .thenReturn(expectedBalance);

        // When - Quando calcular o saldo por data
        BigDecimal result = transactionService.amountByWalletIdAndDate(walletId, dateTime);

        // Then - Então deve retornar o valor correto
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedBalance);
        verify(transactionRepository).amountByWalletIdAndDate(walletId, dateTime);
    }

    @Test
    @DisplayName("Dado um walletId sem transações confirmadas na data, quando calcular saldo por data, então deve retornar zero")
    void dado_walletid_sem_transacoes_quando_calcular_saldo_por_data_entao_deve_retornar_zero() {
        // Given - Dado que não há transações (retorna null)
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

        when(transactionRepository.amountByWalletIdAndDate(walletId, dateTime))
                .thenReturn(null);

        // When - Quando calcular o saldo por data
        BigDecimal result = transactionService.amountByWalletIdAndDate(walletId, dateTime);

        // Then - Então deve retornar zero
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(transactionRepository).amountByWalletIdAndDate(walletId, dateTime);
    }

    @Test
    @DisplayName("Dado um walletId com saldo zero, quando calcular saldo por data, então deve retornar zero")
    void dado_walletid_com_saldo_zero_quando_calcular_saldo_por_data_entao_deve_retornar_zero() {
        // Given - Dado que o saldo calculado é zero
        BigDecimal zeroBalance = BigDecimal.ZERO;
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

        when(transactionRepository.amountByWalletIdAndDate(walletId, dateTime))
                .thenReturn(zeroBalance);

        // When - Quando calcular o saldo por data
        BigDecimal result = transactionService.amountByWalletIdAndDate(walletId, dateTime);

        // Then - Então deve retornar zero
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(transactionRepository).amountByWalletIdAndDate(walletId, dateTime);
    }

    @Test
    @DisplayName("Dado uma transação com valores negativos, quando criar transação, então deve salvar corretamente")
    void dado_transacao_com_valores_negativos_quando_criar_entao_deve_salvar_corretamente() {
        // Given - Dado uma transação com valor negativo (saque)
        BigDecimal negativeAmount = new BigDecimal("-100.00");
        TransactionEntity withdrawTransaction = TransactionEntity.builder()
                .endToEndId("E2E999")
                .walletId(walletId)
                .amount(negativeAmount)
                .type(TransactionTypeEnum.WITHDRAW)
                .status(TransactionStatusEnum.CONFIRMED)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();

        when(transactionRepository.findByEndToEndId("E2E999")).thenReturn(Optional.empty());
        when(transactionRepository.save(withdrawTransaction)).thenReturn(withdrawTransaction);

        // When - Quando criar a transação
        TransactionEntity result = transactionService.create(withdrawTransaction);

        // Then - Então deve salvar corretamente
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(negativeAmount);
        assertThat(result.getType()).isEqualTo(TransactionTypeEnum.WITHDRAW);
        verify(transactionRepository).findByEndToEndId("E2E999");
        verify(transactionRepository).save(withdrawTransaction);
    }

    @Test
    @DisplayName("Dado uma transação com status PENDING, quando criar transação, então deve salvar corretamente")
    void dado_transacao_com_status_pending_quando_criar_entao_deve_salvar_corretamente() {
        // Given - Dado uma transação com status PENDING
        TransactionEntity pendingTransaction = TransactionEntity.builder()
                .endToEndId("E2E888")
                .walletId(walletId)
                .amount(transactionAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.PENDING)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();

        when(transactionRepository.findByEndToEndId("E2E888")).thenReturn(Optional.empty());
        when(transactionRepository.save(pendingTransaction)).thenReturn(pendingTransaction);

        // When - Quando criar a transação
        TransactionEntity result = transactionService.create(pendingTransaction);

        // Then - Então deve salvar corretamente
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatusEnum.PENDING);
        assertThat(result.getType()).isEqualTo(TransactionTypeEnum.PIX_TRANSFER_OUT);
        verify(transactionRepository).findByEndToEndId("E2E888");
        verify(transactionRepository).save(pendingTransaction);
    }

    @Test
    @DisplayName("Dado uma transação com status REJECTED, quando criar transação, então deve salvar corretamente")
    void dado_transacao_com_status_rejected_quando_criar_entao_deve_salvar_corretamente() {
        // Given - Dado uma transação com status REJECTED
        TransactionEntity rejectedTransaction = TransactionEntity.builder()
                .endToEndId("E2E777")
                .walletId(walletId)
                .amount(transactionAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .status(TransactionStatusEnum.REJECTED)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();

        when(transactionRepository.findByEndToEndId("E2E777")).thenReturn(Optional.empty());
        when(transactionRepository.save(rejectedTransaction)).thenReturn(rejectedTransaction);

        // When - Quando criar a transação
        TransactionEntity result = transactionService.create(rejectedTransaction);

        // Then - Então deve salvar corretamente
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatusEnum.REJECTED);
        verify(transactionRepository).findByEndToEndId("E2E777");
        verify(transactionRepository).save(rejectedTransaction);
    }

    @Test
    @DisplayName("Dado múltiplas transações para o mesmo walletId, quando buscar por walletId, então deve retornar todas as transações")
    void dado_multiplas_transacoes_quando_buscar_por_walletid_entao_deve_retornar_todas() {
        // Given - Dado múltiplas transações
        List<TransactionEntity> transactions = Arrays.asList(
                TransactionEntity.builder()
                        .id(1L)
                        .endToEndId("E2E001")
                        .walletId(walletId)
                        .amount(new BigDecimal("100.00"))
                        .type(TransactionTypeEnum.DEPOSIT)
                        .status(TransactionStatusEnum.CONFIRMED)
                        .createdAt(transactionDate)
                        .updatedAt(transactionDate)
                        .build(),
                TransactionEntity.builder()
                        .id(2L)
                        .endToEndId("E2E002")
                        .walletId(walletId)
                        .amount(new BigDecimal("50.00"))
                        .type(TransactionTypeEnum.WITHDRAW)
                        .status(TransactionStatusEnum.CONFIRMED)
                        .createdAt(transactionDate)
                        .updatedAt(transactionDate)
                        .build(),
                TransactionEntity.builder()
                        .id(3L)
                        .endToEndId("E2E003")
                        .walletId(walletId)
                        .amount(new BigDecimal("200.00"))
                        .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                        .status(TransactionStatusEnum.CONFIRMED)
                        .createdAt(transactionDate)
                        .updatedAt(transactionDate)
                        .build()
        );

        when(transactionRepository.findByWalletId(walletId)).thenReturn(Optional.of(transactions));

        // When - Quando buscar por walletId
        List<TransactionEntity> result = transactionService.findByWalletId(walletId);

        // Then - Então deve retornar todas as transações
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsAll(transactions);
        verify(transactionRepository).findByWalletId(walletId);
    }

    @Test
    @DisplayName("Dado uma transação PIX, quando criar transação, então deve salvar corretamente")
    void dado_transacao_pix_quando_criar_entao_deve_salvar_corretamente() {
        // Given - Dado uma transação PIX
        String pixKey = "pix-key-123";
        TransactionEntity pixTransaction = TransactionEntity.builder()
                .endToEndId("E2E666")
                .walletId(walletId)
                .amount(transactionAmount)
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .status(TransactionStatusEnum.PENDING)
                .pixKey(pixKey)
                .createdAt(transactionDate)
                .updatedAt(transactionDate)
                .build();

        when(transactionRepository.findByEndToEndId("E2E666")).thenReturn(Optional.empty());
        when(transactionRepository.save(pixTransaction)).thenReturn(pixTransaction);

        // When - Quando criar a transação
        TransactionEntity result = transactionService.create(pixTransaction);

        // Then - Então deve salvar corretamente
        assertThat(result).isNotNull();
        assertThat(result.getPixKey()).isEqualTo(pixKey);
        assertThat(result.getType()).isEqualTo(TransactionTypeEnum.PIX_TRANSFER_IN);
        verify(transactionRepository).findByEndToEndId("E2E666");
        verify(transactionRepository).save(pixTransaction);
    }
}

