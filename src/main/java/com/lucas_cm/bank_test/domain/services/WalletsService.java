package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;
import com.lucas_cm.bank_test.domain.entities.TransactionStatusEnum;
import com.lucas_cm.bank_test.domain.entities.TransactionTypeEnum;
import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import com.lucas_cm.bank_test.domain.exceptions.InsufficientBalanceException;
import com.lucas_cm.bank_test.domain.exceptions.UserAlreadyHasWalletException;
import com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletsService {

    private final TransactionService transactionService;

    private final WalletRepository walletRepository;

    public WalletEntity create(String userId) {

        var existing = walletRepository.findByUserId(userId);
        if (existing.isPresent()) {
            throw new UserAlreadyHasWalletException();
        }

        WalletEntity wallet = WalletEntity.builder()
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .currentBalance(BigDecimal.ZERO)
                .build();

        return walletRepository.save(wallet);
    }

    public WalletEntity save(WalletEntity wallet) {
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    public WalletEntity insertPixKey(String id, String pixKey) {
        WalletEntity wallet = walletRepository.findById(id)
                .orElseThrow(WalletNotFoundException::new);

        wallet.setPixKey(pixKey);
        wallet.setUpdatedAt(LocalDateTime.now());

        return walletRepository.save(wallet);
    }

    public WalletEntity findById(String id) {
        return walletRepository.findById(id)
                .orElseThrow(WalletNotFoundException::new);
    }

    public WalletEntity findByPixKey(String pixKey) {
        return walletRepository.findByPixKey(pixKey)
                .orElseThrow(WalletNotFoundException::new);
    }

    @Transactional
    public WalletEntity deposit(String walletId, BigDecimal amount) {
        MDC.put("walletId", walletId);
        MDC.put("amount", amount.toString());
        MDC.put("operation", "deposit");

        try {
            log.info("Processando depósito");

            // Usar lock pessimista para prevenir race conditions
            WalletEntity wallet = walletRepository.findByIdWithLock(walletId)
                    .orElseThrow(() -> {
                        log.error("Carteira não encontrada para depósito");
                        return new WalletNotFoundException();
                    });

            wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));

            TransactionEntity transaction = TransactionEntity.builder()
                    .walletId(wallet.getId())
                    .amount(amount)
                    .type(TransactionTypeEnum.DEPOSIT)
                    .status(TransactionStatusEnum.CONFIRMED)
                    .endToEndId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            transactionService.create(transaction);
            walletRepository.save(wallet);

            log.info("Depósito processado com sucesso. Novo saldo: {}", wallet.getCurrentBalance());
            return wallet;
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public WalletEntity withdraw(String walletId, BigDecimal amount) {
        MDC.put("walletId", walletId);
        MDC.put("amount", amount.toString());
        MDC.put("operation", "withdraw");

        try {
            log.info("Processando saque");

            // Usar lock pessimista para prevenir race conditions
            WalletEntity wallet = walletRepository.findByIdWithLock(walletId)
                    .orElseThrow(() -> {
                        log.error("Carteira não encontrada para saque");
                        return new WalletNotFoundException();
                    });

            // Validar saldo (com lock já aplicado, garantindo consistência)
            if (wallet.getCurrentBalance().compareTo(amount) < 0) {
                log.warn("Saldo insuficiente para saque. Saldo atual: {}", wallet.getCurrentBalance());
                throw new InsufficientBalanceException(wallet.getCurrentBalance());
            }

            wallet.setCurrentBalance(wallet.getCurrentBalance().subtract(amount));

            TransactionEntity transaction = TransactionEntity.builder()
                    .walletId(wallet.getId())
                    .amount(amount.negate())
                    .type(TransactionTypeEnum.WITHDRAW)
                    .status(TransactionStatusEnum.CONFIRMED)
                    .endToEndId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            transactionService.create(transaction);
            walletRepository.save(wallet);

            log.info("Saque processado com sucesso. Novo saldo: {}", wallet.getCurrentBalance());
            return wallet;
        } finally {
            MDC.clear();
        }
    }
}
