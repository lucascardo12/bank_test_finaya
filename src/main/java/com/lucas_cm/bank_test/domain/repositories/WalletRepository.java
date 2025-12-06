package com.lucas_cm.bank_test.domain.repositories;

import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {
    Optional<WalletEntity> findByUserId(String id);

    boolean existsByUserId(String id);

    Optional<WalletEntity> findByPixKey(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.id = :id")
    Optional<WalletEntity> findByIdWithLock(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.pixKey = :pixKey")
    Optional<WalletEntity> findByPixKeyWithLock(@Param("pixKey") String pixKey);
}
