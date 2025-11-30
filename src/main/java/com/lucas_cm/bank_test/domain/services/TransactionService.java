package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;
import com.lucas_cm.bank_test.domain.exceptions.TransactionEndToEndIdAlreadyExistsException;
import com.lucas_cm.bank_test.domain.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@AllArgsConstructor
@Service
public class TransactionService {
    private TransactionRepository transactionRepository;

    public TransactionEntity create(TransactionEntity transaction) {
        var findTransaction = transactionRepository.findByEndToEndId(transaction.getEndToEndId());
        if (findTransaction.isPresent()) throw new TransactionEndToEndIdAlreadyExistsException();
        return transactionRepository.save(transaction);
    }

    public List<TransactionEntity> findByWalletId(String walletId) {
        var transactions = transactionRepository.findByWalletId(walletId);
        return transactions.orElseGet(List::of);
    }
}