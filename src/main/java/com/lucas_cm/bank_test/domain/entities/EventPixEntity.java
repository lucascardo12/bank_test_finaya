package com.lucas_cm.bank_test.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "event_pix")
public class EventPixEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    private Long id;
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(name = "occurred_at", nullable = false)
    private String occurredAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "end_to_end_id", nullable = false)
    private String endToEndId;
}
