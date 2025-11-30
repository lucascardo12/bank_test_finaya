package com.lucas_cm.bank_test.domain.repositories;

import com.lucas_cm.bank_test.domain.entities.EventPixEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventPixRepository extends JpaRepository<EventPixEntity, Long> {
    Optional<EventPixEntity> findByEventId(String eventId);
}
