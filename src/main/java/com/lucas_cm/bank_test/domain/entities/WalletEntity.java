package com.lucas_cm.bank_test.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "wallet")
public class WalletEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    private String id;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;
    @Column(name = "pix_key", unique = true)
    private String pixKey;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
