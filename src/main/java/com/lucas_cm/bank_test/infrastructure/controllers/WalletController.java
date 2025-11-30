package com.lucas_cm.bank_test.infrastructure.controllers;


import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import com.lucas_cm.bank_test.domain.services.TransactionService;
import com.lucas_cm.bank_test.domain.services.WalletsService;
import com.lucas_cm.bank_test.infrastructure.dtos.CreateWalletDto;
import com.lucas_cm.bank_test.infrastructure.dtos.DepositDto;
import com.lucas_cm.bank_test.infrastructure.dtos.GetBalanceDto;
import com.lucas_cm.bank_test.infrastructure.dtos.RegisterPixKeyDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@AllArgsConstructor
@RequestMapping("/wallets")
@Tag(name = "Wallets")
public class WalletController {
    WalletsService walletsService;
    TransactionService transactionService;

    @PostMapping
    WalletEntity createWallet(@RequestBody final CreateWalletDto body) {
        return walletsService.create(body.userId());
    }

    @PostMapping(value = "/{id}/pix-keys")
    WalletEntity registerPixKey(@PathVariable final String id, @RequestBody final RegisterPixKeyDto body) {
        return walletsService.insertPixKey(id, body.key());
    }

    @GetMapping(value = "/{id}/balance")
    GetBalanceDto getBalance(
            @PathVariable final String id,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
            LocalDateTime at
    ) {
        var wallet = walletsService.findById(id);
        if (at == null) {
            return new GetBalanceDto(wallet.getId(), wallet.getCurrentBalance());
        }
        var amount = transactionService.amountByWalletIdAndDate(id, at);
        return new GetBalanceDto(id, amount);
    }

    @PostMapping(value = "/{id}/deposit")
    WalletEntity deposit(@PathVariable final String id, @RequestBody final DepositDto body) {
        return walletsService.deposit(id, body.amount());
    }

    @PostMapping(value = "/{id}/withdraw")
    WalletEntity withdraw(@PathVariable final String id, @RequestBody final DepositDto body) {
        return walletsService.withdraw(id, body.amount());
    }
}