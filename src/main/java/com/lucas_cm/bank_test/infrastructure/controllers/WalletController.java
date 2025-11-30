package com.lucas_cm.bank_test.infrastructure.controllers;


import com.lucas_cm.bank_test.domain.entities.WalletEntity;
import com.lucas_cm.bank_test.domain.services.WalletsService;
import com.lucas_cm.bank_test.infrastructure.dtos.CreateWalletDto;
import com.lucas_cm.bank_test.infrastructure.dtos.GetBalanceDto;
import com.lucas_cm.bank_test.infrastructure.dtos.RegisterPixKeyDto;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    WalletsService walletsService;

    @PostMapping
    WalletEntity createWallet(@RequestBody final CreateWalletDto body) {
        return walletsService.create(body.userId());
    }

    @PostMapping(value = "/{id}/pix-keys")
    WalletEntity registerPixKey(@PathVariable final String id, @RequestBody final RegisterPixKeyDto body) {
        return walletsService.insertPixKey(id, body.key());
    }

    @GetMapping(value = "/{id}/balance")
    GetBalanceDto getBalance(@PathVariable final String id) {
        var wallet = walletsService.findById(id);
        return new GetBalanceDto(wallet.getId(), wallet.getCurrentBalance(), new ArrayList<>());
    }
}