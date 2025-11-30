package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.*;
import com.lucas_cm.bank_test.domain.exceptions.InsufficientBalanceException;
import com.lucas_cm.bank_test.domain.exceptions.PixTransferNotFoundException;
import com.lucas_cm.bank_test.domain.repositories.EventPixRepository;
import com.lucas_cm.bank_test.domain.repositories.TransactionRepository;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferRequest;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferResponse;
import com.lucas_cm.bank_test.infrastructure.dtos.PixWebhookRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@AllArgsConstructor
@Service
public class PixService {

    private EventPixRepository eventPixRepository;
    private WalletsService walletsService;
    private TransactionRepository transactionRepository;

    @Transactional
    public PixTransferResponse transfer(String idempotencyKey, PixTransferRequest request) {

        // Verificar idempotência
        Optional<TransactionEntity> existing =
                transactionRepository.findByEndToEndId("OUT" + idempotencyKey);

        if (existing.isPresent()) {
            return new PixTransferResponse(
                    idempotencyKey,
                    existing.get().getStatus()
            );
        }

        // Buscar carteiras
        WalletEntity fromWallet = walletsService.findById(request.fromWalletId());
        WalletEntity toWallet = walletsService.findByPixKey(request.toPixKey());

        // Validar saldo
        if (fromWallet.getCurrentBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(fromWallet.getCurrentBalance());
        }

        LocalDateTime now = LocalDateTime.now();

        // Criar transação de débito (saída)
        TransactionEntity debit = TransactionEntity.builder()
                .walletId(fromWallet.getId())
                .endToEndId("OUT" + idempotencyKey)
                .amount(request.amount().negate())
                .type(TransactionTypeEnum.PIX_TRANSFER_OUT)
                .createdAt(now)
                .updatedAt(now)
                .pixKey(toWallet.getPixKey())
                .status(TransactionStatusEnum.PENDING)
                .build();

        transactionRepository.save(debit);

        // Criar transação de crédito (entrada)
        TransactionEntity credit = TransactionEntity.builder()
                .walletId(toWallet.getId())
                .endToEndId("IN" + idempotencyKey)
                .amount(request.amount())
                .type(TransactionTypeEnum.PIX_TRANSFER_IN)
                .createdAt(now)
                .updatedAt(now)
                .pixKey(toWallet.getPixKey())
                .status(TransactionStatusEnum.PENDING)
                .build();

        transactionRepository.save(credit);

        return new PixTransferResponse(idempotencyKey, TransactionStatusEnum.PENDING);
    }

    @Transactional
    public void processWebhook(PixWebhookRequest request) {

        // Idempotência via eventId
        if (eventPixRepository.findByEventId(request.eventId()).isPresent()) {
            return;
        }

        // Converter data
        LocalDateTime occurredAt = LocalDateTime
                .ofInstant(Instant.parse(request.occurredAt()), ZoneOffset.UTC);

        TransactionStatusEnum status = TransactionStatusEnum.valueOf(request.eventType());

        // Criar evento PIX usando builder
        EventPixEntity event = EventPixEntity.builder()
                .eventId(request.eventId())
                .endToEndId(request.endToEndId())
                .eventType(status)
                .occurredAt(occurredAt)
                .createdAt(LocalDateTime.now())
                .build();

        // Buscar transações associadas
        TransactionEntity debit = transactionRepository
                .findByEndToEndId("OUT" + request.endToEndId())
                .orElseThrow(PixTransferNotFoundException::new);

        TransactionEntity credit = transactionRepository
                .findByEndToEndId("IN" + request.endToEndId())
                .orElseThrow(PixTransferNotFoundException::new);

        if (debit.getStatus() != TransactionStatusEnum.PENDING ||
                credit.getStatus() != TransactionStatusEnum.PENDING) {
            return;
        }

        // Atualizar status
        debit.setStatus(status);
        debit.setUpdatedAt(LocalDateTime.now());

        credit.setStatus(status);
        credit.setUpdatedAt(LocalDateTime.now());

        // Atualizar saldos
        if (status == TransactionStatusEnum.CONFIRMED) {
            updateBalance(debit.getAmount(), debit.getWalletId());
            updateBalance(credit.getAmount(), credit.getWalletId());
        }

        transactionRepository.save(debit);
        transactionRepository.save(credit);
        eventPixRepository.save(event);
    }

    private void updateBalance(BigDecimal amount, String walletId) {
        WalletEntity wallet = walletsService.findById(walletId);
        wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));
        walletsService.save(wallet);
    }
}
