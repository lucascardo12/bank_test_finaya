package com.lucas_cm.bank_test.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    private Long id;
    @Column(name = "end_to_end_id", nullable = false)
    private String endToEndId;
    private double amount;
    private String status;
    private String type;
    @Column(name = "wallet_id", nullable = false)
    private String walletId;
    @Column(name = "pix_key", nullable = false)
    private String pixKey;
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
}
