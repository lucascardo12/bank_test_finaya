package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.EventPixEntity;
import com.lucas_cm.bank_test.domain.exceptions.EventPixIdAlreadyExistsException;
import com.lucas_cm.bank_test.domain.repositories.EventPixRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EventPixService {
    private EventPixRepository eventPixRepository;

    EventPixEntity create(EventPixEntity eventPix) {
        var findEvent = eventPixRepository.findByEventId(eventPix.getEventId());
        if (findEvent.isPresent()) {
            throw new EventPixIdAlreadyExistsException();
        }
        return eventPixRepository.save(eventPix);
    }
}
