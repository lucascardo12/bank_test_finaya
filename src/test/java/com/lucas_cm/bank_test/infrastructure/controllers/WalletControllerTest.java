package com.lucas_cm.bank_test.infrastructure.controllers;

import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import com.lucas_cm.bank_test.domain.services.WalletsService;
import com.lucas_cm.bank_test.infrastructure.dtos.CreateWalletDto;
import com.lucas_cm.bank_test.infrastructure.dtos.DepositDto;
import com.lucas_cm.bank_test.infrastructure.dtos.GetBalanceDto;
import com.lucas_cm.bank_test.infrastructure.dtos.RegisterPixKeyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletController - Testes Unitários")
class WalletControllerTest {

    @Mock
    private WalletsService walletsService;

    @InjectMocks
    private WalletController walletController;

    private String walletId;
    private String userId;
    private WalletEntity walletEntity;
    private BigDecimal balance;

    @BeforeEach
    void setUp() {
        walletId = "wallet-123";
        userId = "user-456";
        balance = new BigDecimal("1000.50");

        walletEntity = WalletEntity.builder()
                .id(walletId)
                .userId(userId)
                .currentBalance(balance)
                .pixKey(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Dado uma requisição válida para criar carteira, quando o endpoint for chamado, então deve retornar a carteira criada")
    void dado_requisicao_valida_quando_criar_carteira_entao_deve_retornar_carteira_criada() {
        // Given - Dado que o serviço retorna uma carteira criada
        CreateWalletDto request = new CreateWalletDto(userId);
        when(walletsService.create(userId)).thenReturn(walletEntity);

        // When - Quando o endpoint de criação for chamado
        WalletEntity response = walletController.createWallet(request);

        // Then - Então deve retornar a carteira criada
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(walletId);
        assertThat(response.getUserId()).isEqualTo(userId);
        verify(walletsService).create(userId);
    }

    @Test
    @DisplayName("Dado uma requisição para criar carteira, quando o endpoint for chamado, então deve chamar o serviço com o userId correto")
    void dado_requisicao_quando_criar_carteira_entao_deve_chamar_servico_com_userid_correto() {
        // Given - Dado uma requisição de criação
        CreateWalletDto request = new CreateWalletDto(userId);
        when(walletsService.create(userId)).thenReturn(walletEntity);

        // When - Quando o endpoint de criação for chamado
        walletController.createWallet(request);

        // Then - Então deve chamar o serviço com o userId correto
        verify(walletsService).create(userId);
    }

    @Test
    @DisplayName("Dado uma requisição válida para registrar chave PIX, quando o endpoint for chamado, então deve retornar a carteira atualizada")
    void dado_requisicao_valida_quando_registrar_chave_pix_entao_deve_retornar_carteira_atualizada() {
        // Given - Dado que o serviço retorna uma carteira com chave PIX registrada
        String pixKey = "pix-key-789";
        RegisterPixKeyDto request = new RegisterPixKeyDto(pixKey, "EMAIL");
        WalletEntity walletWithPixKey = WalletEntity.builder()
                .id(walletId)
                .userId(userId)
                .currentBalance(balance)
                .pixKey(pixKey)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(walletsService.insertPixKey(walletId, pixKey)).thenReturn(walletWithPixKey);

        // When - Quando o endpoint de registro de chave PIX for chamado
        WalletEntity response = walletController.registerPixKey(walletId, request);

        // Then - Então deve retornar a carteira com a chave PIX registrada
        assertThat(response).isNotNull();
        assertThat(response.getPixKey()).isEqualTo(pixKey);
        verify(walletsService).insertPixKey(walletId, pixKey);
    }

    @Test
    @DisplayName("Dado uma requisição para registrar chave PIX, quando o endpoint for chamado, então deve chamar o serviço com os parâmetros corretos")
    void dado_requisicao_quando_registrar_chave_pix_entao_deve_chamar_servico_com_parametros_corretos() {
        // Given - Dado uma requisição de registro de chave PIX
        String pixKey = "pix-key-789";
        RegisterPixKeyDto request = new RegisterPixKeyDto(pixKey, "EMAIL");
        when(walletsService.insertPixKey(walletId, pixKey)).thenReturn(walletEntity);

        // When - Quando o endpoint de registro for chamado
        walletController.registerPixKey(walletId, request);

        // Then - Então deve chamar o serviço com o walletId e a chave PIX corretos
        verify(walletsService).insertPixKey(walletId, pixKey);
    }

    @Test
    @DisplayName("Dado uma requisição para consultar saldo sem parâmetro de data, quando o endpoint for chamado, então deve retornar o saldo atual")
    void dado_requisicao_sem_data_quando_consultar_saldo_entao_deve_retornar_saldo_atual() {
        // Given - Dado que o serviço retorna uma carteira com saldo
        when(walletsService.getBalance(walletId, null)).thenReturn(balance);
        // When - Quando o endpoint de consulta de saldo for chamado sem parâmetro "at"
        GetBalanceDto response = walletController.getBalance(walletId, null);

        // Then - Então deve retornar o saldo atual da carteira
        assertThat(response).isNotNull();
        assertThat(response.walletId()).isEqualTo(walletId);
        assertThat(response.currentBalance()).isEqualTo(balance);
        verify(walletsService).getBalance(walletId, null);
    }

    @Test
    @DisplayName("Dado uma requisição para consultar saldo com parâmetro de data, quando o endpoint for chamado, então deve retornar o saldo histórico")
    void dado_requisicao_com_data_quando_consultar_saldo_entao_deve_retornar_saldo_historico() {
        // Given - Dado uma data e um saldo histórico
        String dateTimeString = "2025-01-01T10:00:00Z";
        BigDecimal historicalBalance = new BigDecimal("500.25");

        when(walletsService.getBalance(walletId, dateTimeString)).thenReturn(historicalBalance);

        // When - Quando o endpoint de consulta de saldo for chamado com parâmetro "at"
        GetBalanceDto response = walletController.getBalance(walletId, dateTimeString);

        // Then - Então deve retornar o saldo histórico calculado
        assertThat(response).isNotNull();
        assertThat(response.walletId()).isEqualTo(walletId);
        assertThat(response.currentBalance()).isEqualTo(historicalBalance);
        verify(walletsService).getBalance(eq(walletId), eq(dateTimeString));
    }

    @Test
    @DisplayName("Dado uma requisição para consultar saldo com data, quando o endpoint for chamado, então deve converter a data ISO corretamente")
    void dado_requisicao_com_data_quando_consultar_saldo_entao_deve_converter_data_iso_corretamente() {
        // Given - Dado uma data ISO com timezone UTC
        String dateTimeString = "2025-06-15T14:30:00Z";
        BigDecimal historicalBalance = new BigDecimal("750.00");

        when(walletsService.getBalance(eq(walletId), any()))
                .thenReturn(historicalBalance);

        // When - Quando o endpoint de consulta de saldo for chamado
        walletController.getBalance(walletId, dateTimeString);

        // Then - Então deve converter a string ISO para LocalDateTime corretamente
        verify(walletsService).getBalance(eq(walletId), any());
    }

    @Test
    @DisplayName("Dado uma requisição válida para depósito, quando o endpoint for chamado, então deve retornar a carteira atualizada")
    void dado_requisicao_valida_quando_fazer_deposito_entao_deve_retornar_carteira_atualizada() {
        // Given - Dado um valor de depósito e uma carteira atualizada
        BigDecimal depositAmount = new BigDecimal("200.00");
        DepositDto request = new DepositDto(depositAmount);
        BigDecimal newBalance = balance.add(depositAmount);

        WalletEntity updatedWallet = WalletEntity.builder()
                .id(walletId)
                .userId(userId)
                .currentBalance(newBalance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(walletsService.deposit(walletId, depositAmount)).thenReturn(updatedWallet);

        // When - Quando o endpoint de depósito for chamado
        WalletEntity response = walletController.deposit(walletId, request);

        // Then - Então deve retornar a carteira com o saldo atualizado
        assertThat(response).isNotNull();
        assertThat(response.getCurrentBalance()).isEqualTo(newBalance);
        verify(walletsService).deposit(walletId, depositAmount);
    }

    @Test
    @DisplayName("Dado uma requisição para depósito, quando o endpoint for chamado, então deve chamar o serviço com os parâmetros corretos")
    void dado_requisicao_quando_fazer_deposito_entao_deve_chamar_servico_com_parametros_corretos() {
        // Given - Dado uma requisição de depósito
        BigDecimal depositAmount = new BigDecimal("150.75");
        DepositDto request = new DepositDto(depositAmount);
        when(walletsService.deposit(walletId, depositAmount)).thenReturn(walletEntity);

        // When - Quando o endpoint de depósito for chamado
        walletController.deposit(walletId, request);

        // Then - Então deve chamar o serviço com o walletId e o valor corretos
        verify(walletsService).deposit(walletId, depositAmount);
    }

    @Test
    @DisplayName("Dado uma requisição válida para saque, quando o endpoint for chamado, então deve retornar a carteira atualizada")
    void dado_requisicao_valida_quando_fazer_saque_entao_deve_retornar_carteira_atualizada() {
        // Given - Dado um valor de saque e uma carteira atualizada
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        DepositDto request = new DepositDto(withdrawAmount);
        BigDecimal newBalance = balance.subtract(withdrawAmount);

        WalletEntity updatedWallet = WalletEntity.builder()
                .id(walletId)
                .userId(userId)
                .currentBalance(newBalance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(walletsService.withdraw(walletId, withdrawAmount)).thenReturn(updatedWallet);

        // When - Quando o endpoint de saque for chamado
        WalletEntity response = walletController.withdraw(walletId, request);

        // Then - Então deve retornar a carteira com o saldo atualizado
        assertThat(response).isNotNull();
        assertThat(response.getCurrentBalance()).isEqualTo(newBalance);
        verify(walletsService).withdraw(walletId, withdrawAmount);
    }

    @Test
    @DisplayName("Dado uma requisição para saque, quando o endpoint for chamado, então deve chamar o serviço com os parâmetros corretos")
    void dado_requisicao_quando_fazer_saque_entao_deve_chamar_servico_com_parametros_corretos() {
        // Given - Dado uma requisição de saque
        BigDecimal withdrawAmount = new BigDecimal("50.25");
        DepositDto request = new DepositDto(withdrawAmount);
        when(walletsService.withdraw(walletId, withdrawAmount)).thenReturn(walletEntity);

        // When - Quando o endpoint de saque for chamado
        walletController.withdraw(walletId, request);

        // Then - Então deve chamar o serviço com o walletId e o valor corretos
        verify(walletsService).withdraw(walletId, withdrawAmount);
    }
}

