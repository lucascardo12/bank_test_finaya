package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import com.lucas_cm.bank_test.domain.exceptions.UserAlreadyHasWalletException;
import com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class WalletsService {
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

}
