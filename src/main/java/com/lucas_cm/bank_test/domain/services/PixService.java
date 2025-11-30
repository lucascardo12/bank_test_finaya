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
        // Verificar se já existe uma operação com a mesma idempotency key
        Optional<TransactionEntity> existing =
                transactionRepository.findByEndToEndId("OUT" + idempotencyKey);

        if (existing.isPresent()) {
            return new PixTransferResponse(
                    idempotencyKey,
                    existing.get().getStatus()
            );
        }

        // Buscar wallet origem
        WalletEntity fromWallet = walletsService.findById(request.fromWalletId());
        // Busca a chave destino
        WalletEntity toWallet = walletsService.findByPixKey(request.toPixKey());

        // Verificar saldo
        if (fromWallet.getCurrentBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(fromWallet.getCurrentBalance());
        }

        // Criar transações
        TransactionEntity debit = new TransactionEntity();
        debit.setWalletId(fromWallet.getId());
        debit.setEndToEndId("OUT" + idempotencyKey);
        debit.setAmount(request.amount().negate());
        debit.setType(TransactionTypeEnum.PIX_TRANSFER_OUT);
        debit.setCreatedAt(LocalDateTime.now());
        debit.setUpdatedAt(LocalDateTime.now());
        debit.setPixKey(toWallet.getPixKey());
        debit.setStatus(TransactionStatusEnum.PENDING);
        transactionRepository.save(debit);

        TransactionEntity credit = new TransactionEntity();
        credit.setWalletId(toWallet.getId());
        credit.setEndToEndId("IN" + idempotencyKey);
        credit.setAmount(request.amount());
        credit.setType(TransactionTypeEnum.PIX_TRANSFER_IN);
        credit.setCreatedAt(LocalDateTime.now());
        credit.setUpdatedAt(LocalDateTime.now());
        credit.setPixKey(toWallet.getPixKey());
        credit.setStatus(TransactionStatusEnum.PENDING);
        transactionRepository.save(credit);

        return new PixTransferResponse(idempotencyKey, TransactionStatusEnum.PENDING);
    }

    @Transactional
    public void processWebhook(PixWebhookRequest request) {
        // Idempotência — se já existe, não processa de novo
        var existing = eventPixRepository.findByEventId(request.eventId());

        if (existing.isPresent()) {
            return;
        }
        // Registrar evento para garantir idempotência
        var event = new EventPixEntity();
        var status = TransactionStatusEnum.valueOf(request.eventType());
        event.setEventId(request.eventId());
        event.setEndToEndId(request.endToEndId());
        event.setEventType(status);
        Instant instant = Instant.parse(request.occurredAt());
        LocalDateTime occurredAt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        event.setOccurredAt(occurredAt);
        event.setCreatedAt(LocalDateTime.now());

        // Buscar transferência PIX pelo endToEndId
        var debit = transactionRepository
                .findByEndToEndId("OUT" + request.endToEndId())
                .orElseThrow(PixTransferNotFoundException::new);
        var credit = transactionRepository
                .findByEndToEndId("IN" + request.endToEndId())
                .orElseThrow(PixTransferNotFoundException::new);

        if (debit.getStatus() != TransactionStatusEnum.PENDING) {
            return;
        }
        if (credit.getStatus() != TransactionStatusEnum.PENDING) {
            return;
        }
        //atualiza o status das transações
        debit.setStatus(status);
        debit.setUpdatedAt(LocalDateTime.now());
        credit.setStatus(status);
        credit.setUpdatedAt(LocalDateTime.now());

        // caso transação confirmada atualiza o balance atual das carteiras
        if (status == TransactionStatusEnum.CONFIRMED) {
            updateBalance(debit.getAmount(), debit.getWalletId(), true);
            updateBalance(credit.getAmount(), credit.getWalletId(), false);
        }

        transactionRepository.save(debit);
        transactionRepository.save(credit);
        eventPixRepository.save(event);
    }

    private void updateBalance(BigDecimal amount, String walletId, Boolean isOut) {
        WalletEntity fromWallet = walletsService.findById(walletId);
        fromWallet.setCurrentBalance(
                fromWallet.getCurrentBalance().add(amount)
        );
        walletsService.save(fromWallet);
    }
}
