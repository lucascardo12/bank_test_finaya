package com.lucas_cm.bank_test.domain.services;

import com.lucas_cm.bank_test.domain.entities.*;
import com.lucas_cm.bank_test.domain.exceptions.InsufficientBalanceException;
import com.lucas_cm.bank_test.domain.exceptions.PixTransferNotFoundException;
import com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException;
import com.lucas_cm.bank_test.domain.repositories.EventPixRepository;
import com.lucas_cm.bank_test.domain.repositories.TransactionRepository;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferRequest;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferResponse;
import com.lucas_cm.bank_test.infrastructure.dtos.PixWebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PixService {
    private final EventPixRepository eventPixRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public PixTransferResponse transfer(String idempotencyKey, PixTransferRequest request) {
        // Adicionar contexto de log estruturado
        MDC.put("idempotencyKey", idempotencyKey);
        MDC.put("fromWalletId", request.fromWalletId());
        MDC.put("toPixKey", request.toPixKey());
        MDC.put("amount", request.amount().toString());

        try {
            log.info("Iniciando transferência PIX");

            // Verificar idempotência
            Optional<TransactionEntity> existing =
                    transactionRepository.findByEndToEndId("OUT" + idempotencyKey);

            if (existing.isPresent()) {
                log.info("Transferência já processada (idempotência), retornando resultado existente");
                MDC.put("endToEndId", idempotencyKey);
                return new PixTransferResponse(
                        idempotencyKey,
                        existing.get().getStatus()
                );
            }

            // Buscar carteiras com lock pessimista para prevenir race conditions
            WalletEntity fromWallet = walletRepository.findByIdWithLock(request.fromWalletId())
                    .orElseThrow(() -> {
                        log.error("Carteira origem não encontrada");
                        return new WalletNotFoundException();
                    });

            WalletEntity toWallet = walletRepository.findByPixKeyWithLock(request.toPixKey())
                    .orElseThrow(() -> {
                        log.error("Carteira destino não encontrada para chave PIX");
                        return new WalletNotFoundException();
                    });

            // Validar saldo (com lock já aplicado)
            if (fromWallet.getCurrentBalance().compareTo(request.amount()) < 0) {
                log.warn("Saldo insuficiente para transferência");
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

            log.info("Transferência PIX criada com sucesso");
            MDC.put("endToEndId", idempotencyKey);
            return new PixTransferResponse(idempotencyKey, TransactionStatusEnum.PENDING);
        } finally {
            // Limpar contexto MDC
            MDC.clear();
        }
    }

    @Transactional
    public void processWebhook(PixWebhookRequest request) {
        // Adicionar contexto de log estruturado
        MDC.put("eventId", request.eventId());
        MDC.put("endToEndId", request.endToEndId());
        MDC.put("eventType", request.eventType());

        try {
            log.info("Processando webhook PIX");

            // Idempotência via eventId
            Optional<EventPixEntity> existingEvent = eventPixRepository.findByEventId(request.eventId());
            if (existingEvent.isPresent()) {
                log.info("Evento já processado (idempotência), ignorando");
                return;
            }

            // Converter data
            LocalDateTime occurredAt = LocalDateTime
                    .ofInstant(Instant.parse(request.occurredAt()), ZoneOffset.UTC);

            TransactionStatusEnum newStatus = TransactionStatusEnum.valueOf(request.eventType());

            // Buscar transações associadas
            TransactionEntity debit = transactionRepository
                    .findByEndToEndId("OUT" + request.endToEndId())
                    .orElseThrow(() -> {
                        log.error("Transação de débito não encontrada");
                        return new PixTransferNotFoundException();
                    });

            TransactionEntity credit = transactionRepository
                    .findByEndToEndId("IN" + request.endToEndId())
                    .orElseThrow(() -> {
                        log.error("Transação de crédito não encontrada");
                        return new PixTransferNotFoundException();
                    });

            // Validação de máquina de estados
            // Não permitir mudanças se já estiver CONFIRMED ou REJECTED
            if (debit.getStatus() == TransactionStatusEnum.CONFIRMED ||
                    credit.getStatus() == TransactionStatusEnum.CONFIRMED) {
                if (newStatus == TransactionStatusEnum.REJECTED) {
                    log.warn("Tentativa de rejeitar transação já confirmada, ignorando");
                    return;
                }
                // Se já está CONFIRMED e recebe outro CONFIRMED, é idempotente mas já processado
                log.info("Transação já confirmada, ignorando evento");
                return;
            }

            if (debit.getStatus() == TransactionStatusEnum.REJECTED ||
                    credit.getStatus() == TransactionStatusEnum.REJECTED) {
                if (newStatus == TransactionStatusEnum.CONFIRMED) {
                    log.warn("Tentativa de confirmar transação já rejeitada, ignorando");
                    return;
                }
                // Se já está REJECTED e recebe outro REJECTED, é idempotente mas já processado
                log.info("Transação já rejeitada, ignorando evento");
                return;
            }

            // Só processa se estiver PENDING
            if (debit.getStatus() != TransactionStatusEnum.PENDING ||
                    credit.getStatus() != TransactionStatusEnum.PENDING) {
                log.warn("Transação em estado inválido para processamento: debit={}, credit={}",
                        debit.getStatus(), credit.getStatus());
                return;
            }

            // Criar evento PIX usando builder
            EventPixEntity event = EventPixEntity.builder()
                    .eventId(request.eventId())
                    .endToEndId(request.endToEndId())
                    .eventType(newStatus)
                    .occurredAt(occurredAt)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Atualizar status
            debit.setStatus(newStatus);
            debit.setUpdatedAt(LocalDateTime.now());

            credit.setStatus(newStatus);
            credit.setUpdatedAt(LocalDateTime.now());

            // Atualizar saldos apenas quando CONFIRMED
            if (newStatus == TransactionStatusEnum.CONFIRMED) {
                updateBalance(debit.getAmount(), debit.getWalletId());
                updateBalance(credit.getAmount(), credit.getWalletId());
                log.info("Transferência PIX confirmada e saldos atualizados");
            } else if (newStatus == TransactionStatusEnum.REJECTED) {
                log.info("Transferência PIX rejeitada");
            }

            transactionRepository.save(debit);
            transactionRepository.save(credit);
            eventPixRepository.save(event);
        } finally {
            // Limpar contexto MDC
            MDC.clear();
        }
    }

    private void updateBalance(BigDecimal amount, String walletId) {
        // Usar lock pessimista para atualizar saldo
        WalletEntity wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> {
                    log.error("Carteira não encontrada para atualização de saldo");
                    return new com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException();
                });
        wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));
        walletRepository.save(wallet);
        log.debug("Saldo atualizado: walletId={}, amount={}, newBalance={}",
                walletId, amount, wallet.getCurrentBalance());
    }
}
