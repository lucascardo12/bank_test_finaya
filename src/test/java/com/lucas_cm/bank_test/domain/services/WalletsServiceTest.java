package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;
import com.lucas_cm.bank_test.domain.entities.TransactionStatusEnum;
import com.lucas_cm.bank_test.domain.entities.TransactionTypeEnum;
import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import com.lucas_cm.bank_test.domain.exceptions.InsufficientBalanceException;
import com.lucas_cm.bank_test.domain.exceptions.UserAlreadyHasWalletException;
import com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
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
@DisplayName("WalletsService - Testes Unitários")
class WalletsServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletsService walletsService;

    private String walletId;
    private String userId;
    private String pixKey;
    private WalletEntity walletEntity;
    private BigDecimal initialBalance;

    @BeforeEach
    void setUp() {
        walletId = "wallet-123";
        userId = "user-456";
        pixKey = "pix-key-789";
        initialBalance = new BigDecimal("1000.00");

        walletEntity = WalletEntity.builder()
                .id(walletId)
                .userId(userId)
                .currentBalance(initialBalance)
                .pixKey(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Dado um userId válido sem carteira existente, quando criar carteira, então deve criar e retornar carteira com saldo zero")
    void dado_userid_valido_sem_carteira_quando_criar_entao_deve_criar_carteira_com_saldo_zero() {
        // Given - Dado que o usuário não possui carteira
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity wallet = invocation.getArgument(0);
            wallet.setId(walletId);
            return wallet;
        });

        // When - Quando criar a carteira
        WalletEntity result = walletsService.create(userId);

        // Then - Então deve criar carteira com saldo zero
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCurrentBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um userId que já possui carteira, quando criar carteira, então deve lançar exceção UserAlreadyHasWalletException")
    void dado_userid_com_carteira_quando_criar_entao_deve_lancar_excecao() {
        // Given - Dado que o usuário já possui carteira
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(walletEntity));

        // When/Then - Quando criar a carteira, então deve lançar exceção
        assertThatThrownBy(() -> walletsService.create(userId))
                .isInstanceOf(UserAlreadyHasWalletException.class);

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado uma carteira válida, quando salvar, então deve atualizar updatedAt e retornar carteira salva")
    void dado_carteira_valida_quando_salvar_entao_deve_atualizar_updated_at() {
        // Given - Dado uma carteira
        LocalDateTime originalUpdatedAt = walletEntity.getUpdatedAt();
        when(walletRepository.save(walletEntity)).thenReturn(walletEntity);

        // When - Quando salvar a carteira
        WalletEntity result = walletsService.save(walletEntity);

        // Then - Então deve atualizar updatedAt e salvar
        assertThat(result).isNotNull();
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        verify(walletRepository).save(walletEntity);
    }

    @Test
    @DisplayName("Dado um walletId e chave PIX válidos, quando inserir chave PIX, então deve atualizar carteira com chave PIX")
    void dado_walletid_e_chave_pix_validos_quando_inserir_chave_pix_entao_deve_atualizar_carteira() {
        // Given - Dado que a carteira existe
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(walletEntity));
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity wallet = invocation.getArgument(0);
            wallet.setPixKey(pixKey);
            return wallet;
        });

        // When - Quando inserir a chave PIX
        WalletEntity result = walletsService.insertPixKey(walletId, pixKey);

        // Then - Então deve atualizar a carteira com a chave PIX
        assertThat(result).isNotNull();
        assertThat(result.getPixKey()).isEqualTo(pixKey);
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um walletId inexistente, quando inserir chave PIX, então deve lançar exceção WalletNotFoundException")
    void dado_walletid_inexistente_quando_inserir_chave_pix_entao_deve_lancar_excecao() {
        // Given - Dado que a carteira não existe
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When/Then - Quando inserir a chave PIX, então deve lançar exceção
        assertThatThrownBy(() -> walletsService.insertPixKey(walletId, pixKey))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um walletId válido, quando buscar por ID, então deve retornar a carteira")
    void dado_walletid_valido_quando_buscar_por_id_entao_deve_retornar_carteira() {
        // Given - Dado que a carteira existe
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(walletEntity));

        // When - Quando buscar por ID
        WalletEntity result = walletsService.findById(walletId);

        // Then - Então deve retornar a carteira
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(walletId);
        verify(walletRepository).findById(walletId);
    }

    @Test
    @DisplayName("Dado um walletId inexistente, quando buscar por ID, então deve lançar exceção WalletNotFoundException")
    void dado_walletid_inexistente_quando_buscar_por_id_entao_deve_lancar_excecao() {
        // Given - Dado que a carteira não existe
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When/Then - Quando buscar por ID, então deve lançar exceção
        assertThatThrownBy(() -> walletsService.findById(walletId))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findById(walletId);
    }

    @Test
    @DisplayName("Dado uma chave PIX válida, quando buscar por chave PIX, então deve retornar a carteira")
    void dado_chave_pix_valida_quando_buscar_por_chave_pix_entao_deve_retornar_carteira() {
        // Given - Dado que a carteira existe com a chave PIX
        walletEntity.setPixKey(pixKey);
        when(walletRepository.findByPixKey(pixKey)).thenReturn(Optional.of(walletEntity));

        // When - Quando buscar por chave PIX
        WalletEntity result = walletsService.findByPixKey(pixKey);

        // Then - Então deve retornar a carteira
        assertThat(result).isNotNull();
        assertThat(result.getPixKey()).isEqualTo(pixKey);
        verify(walletRepository).findByPixKey(pixKey);
    }

    @Test
    @DisplayName("Dado uma chave PIX inexistente, quando buscar por chave PIX, então deve lançar exceção WalletNotFoundException")
    void dado_chave_pix_inexistente_quando_buscar_por_chave_pix_entao_deve_lancar_excecao() {
        // Given - Dado que a carteira não existe
        when(walletRepository.findByPixKey(pixKey)).thenReturn(Optional.empty());

        // When/Then - Quando buscar por chave PIX, então deve lançar exceção
        assertThatThrownBy(() -> walletsService.findByPixKey(pixKey))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findByPixKey(pixKey);
    }

    @Test
    @DisplayName("Dado um depósito válido, quando realizar depósito, então deve aumentar saldo e criar transação")
    void dado_deposito_valido_quando_realizar_deposito_entao_deve_aumentar_saldo_e_criar_transacao() {
        // Given - Dado um valor de depósito
        BigDecimal depositAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = initialBalance.add(depositAmount);

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(walletEntity));
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity wallet = invocation.getArgument(0);
            return wallet;
        });
        when(transactionService.create(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity transaction = invocation.getArgument(0);
            return transaction;
        });

        // When - Quando realizar o depósito
        WalletEntity result = walletsService.deposit(walletId, depositAmount);

        // Then - Então deve aumentar o saldo
        assertThat(result).isNotNull();
        assertThat(result.getCurrentBalance()).isEqualTo(expectedBalance);

        // Verificar que a transação foi criada corretamente
        ArgumentCaptor<TransactionEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionService).create(transactionCaptor.capture());
        TransactionEntity createdTransaction = transactionCaptor.getValue();

        assertThat(createdTransaction.getWalletId()).isEqualTo(walletId);
        assertThat(createdTransaction.getAmount()).isEqualTo(depositAmount);
        assertThat(createdTransaction.getType()).isEqualTo(TransactionTypeEnum.DEPOSIT);
        assertThat(createdTransaction.getStatus()).isEqualTo(TransactionStatusEnum.CONFIRMED);
        assertThat(createdTransaction.getEndToEndId()).isNotNull();

        verify(walletRepository).findByIdWithLock(walletId);
        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um walletId inexistente, quando realizar depósito, então deve lançar exceção WalletNotFoundException")
    void dado_walletid_inexistente_quando_realizar_deposito_entao_deve_lancar_excecao() {
        // Given - Dado que a carteira não existe
        BigDecimal depositAmount = new BigDecimal("200.00");
        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.empty());

        // When/Then - Quando realizar o depósito, então deve lançar exceção
        assertThatThrownBy(() -> walletsService.deposit(walletId, depositAmount))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findByIdWithLock(walletId);
        verify(transactionService, never()).create(any(TransactionEntity.class));
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um saque válido com saldo suficiente, quando realizar saque, então deve diminuir saldo e criar transação")
    void dado_saque_valido_com_saldo_suficiente_quando_realizar_saque_entao_deve_diminuir_saldo_e_criar_transacao() {
        // Given - Dado um valor de saque menor que o saldo
        BigDecimal withdrawAmount = new BigDecimal("300.00");
        BigDecimal expectedBalance = initialBalance.subtract(withdrawAmount);

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(walletEntity));
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity wallet = invocation.getArgument(0);
            return wallet;
        });
        when(transactionService.create(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity transaction = invocation.getArgument(0);
            return transaction;
        });

        // When - Quando realizar o saque
        WalletEntity result = walletsService.withdraw(walletId, withdrawAmount);

        // Then - Então deve diminuir o saldo
        assertThat(result).isNotNull();
        assertThat(result.getCurrentBalance()).isEqualTo(expectedBalance);

        // Verificar que a transação foi criada corretamente
        ArgumentCaptor<TransactionEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionService).create(transactionCaptor.capture());
        TransactionEntity createdTransaction = transactionCaptor.getValue();

        assertThat(createdTransaction.getWalletId()).isEqualTo(walletId);
        assertThat(createdTransaction.getAmount()).isEqualTo(withdrawAmount.negate());
        assertThat(createdTransaction.getType()).isEqualTo(TransactionTypeEnum.WITHDRAW);
        assertThat(createdTransaction.getStatus()).isEqualTo(TransactionStatusEnum.CONFIRMED);
        assertThat(createdTransaction.getEndToEndId()).isNotNull();

        verify(walletRepository).findByIdWithLock(walletId);
        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um saque com saldo insuficiente, quando realizar saque, então deve lançar exceção InsufficientBalanceException")
    void dado_saque_com_saldo_insuficiente_quando_realizar_saque_entao_deve_lancar_excecao() {
        // Given - Dado um valor de saque maior que o saldo
        BigDecimal withdrawAmount = new BigDecimal("2000.00");

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(walletEntity));

        // When/Then - Quando realizar o saque, então deve lançar exceção
        assertThatThrownBy(() -> walletsService.withdraw(walletId, withdrawAmount))
                .isInstanceOf(InsufficientBalanceException.class)
                .satisfies(exception -> {
                    InsufficientBalanceException ex = (InsufficientBalanceException) exception;
                    assertThat(ex.getData()).isNotNull();
                });

        verify(walletRepository).findByIdWithLock(walletId);
        verify(transactionService, never()).create(any(TransactionEntity.class));
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um saque com saldo igual ao valor, quando realizar saque, então deve permitir o saque")
    void dado_saque_com_saldo_igual_ao_valor_quando_realizar_saque_entao_deve_permitir_saque() {
        // Given - Dado um valor de saque igual ao saldo
        BigDecimal withdrawAmount = initialBalance;
        BigDecimal expectedBalance = BigDecimal.ZERO;

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(walletEntity));
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity wallet = invocation.getArgument(0);
            return wallet;
        });
        when(transactionService.create(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity transaction = invocation.getArgument(0);
            return transaction;
        });

        // When - Quando realizar o saque
        WalletEntity result = walletsService.withdraw(walletId, withdrawAmount);

        // Then - Então deve permitir o saque e zerar o saldo
        assertThat(result).isNotNull();
        assertThat(result.getCurrentBalance()).isEqualByComparingTo(expectedBalance);
        verify(walletRepository).findByIdWithLock(walletId);
        verify(transactionService).create(any(TransactionEntity.class));
        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um walletId inexistente, quando realizar saque, então deve lançar exceção WalletNotFoundException")
    void dado_walletid_inexistente_quando_realizar_saque_entao_deve_lancar_excecao() {
        // Given - Dado que a carteira não existe
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.empty());

        // When/Then - Quando realizar o saque, então deve lançar exceção
        assertThatThrownBy(() -> walletsService.withdraw(walletId, withdrawAmount))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findByIdWithLock(walletId);
        verify(transactionService, never()).create(any(TransactionEntity.class));
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    @DisplayName("Dado um depósito, quando realizar depósito, então deve atualizar updatedAt da carteira")
    void dado_deposito_quando_realizar_deposito_entao_deve_atualizar_updated_at() {
        // Given - Dado um depósito
        BigDecimal depositAmount = new BigDecimal("100.00");
        LocalDateTime originalUpdatedAt = walletEntity.getUpdatedAt();

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(walletEntity));
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity wallet = invocation.getArgument(0);
            return wallet;
        });
        when(transactionService.create(any(TransactionEntity.class))).thenReturn(mock(TransactionEntity.class));

        // When - Quando realizar o depósito
        walletsService.deposit(walletId, depositAmount);

        // Then - Então deve atualizar updatedAt
        ArgumentCaptor<WalletEntity> walletCaptor = ArgumentCaptor.forClass(WalletEntity.class);
        verify(walletRepository).save(walletCaptor.capture());
        WalletEntity savedWallet = walletCaptor.getValue();
        assertThat(savedWallet.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    @DisplayName("Dado um saque, quando realizar saque, então deve atualizar updatedAt da carteira")
    void dado_saque_quando_realizar_saque_entao_deve_atualizar_updated_at() {
        // Given - Dado um saque
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        LocalDateTime originalUpdatedAt = walletEntity.getUpdatedAt();

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(walletEntity));
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity wallet = invocation.getArgument(0);
            return wallet;
        });
        when(transactionService.create(any(TransactionEntity.class))).thenReturn(mock(TransactionEntity.class));

        // When - Quando realizar o saque
        walletsService.withdraw(walletId, withdrawAmount);

        // Then - Então deve atualizar updatedAt
        ArgumentCaptor<WalletEntity> walletCaptor = ArgumentCaptor.forClass(WalletEntity.class);
        verify(walletRepository).save(walletCaptor.capture());
        WalletEntity savedWallet = walletCaptor.getValue();
        assertThat(savedWallet.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}

