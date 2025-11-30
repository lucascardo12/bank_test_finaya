package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;
import com.lucas_cm.bank_test.domain.entities.TransactionStatusEnum;
import com.lucas_cm.bank_test.domain.entities.TransactionTypeEnum;
import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import com.lucas_cm.bank_test.domain.exceptions.InsufficientBalanceException;
import com.lucas_cm.bank_test.domain.exceptions.UserAlreadyHasWalletException;
import com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class WalletsService {
    private TransactionService transactionService;
    private WalletRepository walletRepository;

    public WalletEntity create(String userId) {
        var findWallet = walletRepository.findByUserId(userId);
        if (findWallet.isPresent()) throw new UserAlreadyHasWalletException();
        var wallet = new WalletEntity();
        wallet.setUserId(userId);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        wallet.setCurrentBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    public WalletEntity save(WalletEntity wallet) {
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    public WalletEntity insertPixKey(String id, String pixKey) {
        var findWallet = walletRepository.findById(id);
        if (findWallet.isEmpty()) throw new WalletNotFoundException();
        var wallet = findWallet.get();
        wallet.setPixKey(pixKey);
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    public WalletEntity findById(String id) {
        var findWallet = walletRepository.findById(id);
        if (findWallet.isEmpty()) throw new WalletNotFoundException();
        return findWallet.get();
    }

    public WalletEntity findByPixKey(String pixKey) {
        var findWallet = walletRepository.findByPixKey(pixKey);
        if (findWallet.isEmpty()) throw new WalletNotFoundException();
        return findWallet.get();
    }

    @Transactional
    public WalletEntity deposit(String walletId, BigDecimal amount) {
        WalletEntity wallet = findById(walletId);

        // Atualizar saldo
        wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));

        // Criar transação
        TransactionEntity transaction = new TransactionEntity();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount);
        transaction.setType(TransactionTypeEnum.DEPOSIT);
        transaction.setStatus(TransactionStatusEnum.CONFIRMED);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setEndToEndId(UUID.randomUUID().toString());

        // Salvar transação
        transactionService.create(transaction);

        // Salvar wallet com novo saldo
        walletRepository.save(wallet);

        return wallet;
    }

    @Transactional
    public WalletEntity withdraw(String walletId, BigDecimal amount) {
        WalletEntity wallet = findById(walletId);

        //Verificar saldo suficiente
        if (wallet.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(wallet.getCurrentBalance());
        }

        //Atualizar saldo
        wallet.setCurrentBalance(wallet.getCurrentBalance().subtract(amount));

        //Criar transação de saque
        TransactionEntity transaction = new TransactionEntity();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount.negate());
        transaction.setType(TransactionTypeEnum.WITHDRAW);
        transaction.setEndToEndId(UUID.randomUUID().toString());
        transaction.setStatus(TransactionStatusEnum.CONFIRMED);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        // 5. Salvar transação
        transactionService.create(transaction);

        // 6. Atualizar wallet
        walletRepository.save(wallet);

        return wallet;
    }


}
