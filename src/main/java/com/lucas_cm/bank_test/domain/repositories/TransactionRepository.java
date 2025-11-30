package com.lucas_cm.bank_test.domain.repositories;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    Optional<List<TransactionEntity>> findByWalletId(String walletId);

    Optional<TransactionEntity> findByEndToEndId(String endToEndId);
}
