package com.lucas_cm.bank_test.domain.repositories;

import com.lucas_cm.bank_test.domain.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    Optional<List<TransactionEntity>> findByWalletId(String walletId);

    Optional<TransactionEntity> findByEndToEndId(String endToEndId);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t " +
            "WHERE t.walletId = :walletId " +
            "AND t.createdAt >= :at " +
            "AND t.status = 'CONFIRMED' ")
    BigDecimal amountByWalletIdAndDate(
            @Param("walletId") String walletId,
            @Param("at") LocalDateTime at
    );
}
