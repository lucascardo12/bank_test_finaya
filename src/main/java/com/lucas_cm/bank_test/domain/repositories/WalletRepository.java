package com.lucas_cm.bank_test.domain.repositories;

import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {
    Optional<WalletEntity> findByUserId(String id);
}
