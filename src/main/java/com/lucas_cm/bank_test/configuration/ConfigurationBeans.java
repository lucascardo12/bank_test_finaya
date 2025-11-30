package com.lucas_cm.bank_test.configuration;

import com.lucas_cm.bank_test.domain.repositories.EventPixRepository;
import com.lucas_cm.bank_test.domain.repositories.TransactionRepository;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
import com.lucas_cm.bank_test.domain.services.PixService;
import com.lucas_cm.bank_test.domain.services.TransactionService;
import com.lucas_cm.bank_test.domain.services.WalletsService;
import com.lucas_cm.bank_test.infrastructure.controllers.PixController;
import com.lucas_cm.bank_test.infrastructure.controllers.WalletController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationBeans {

    @Bean
    public WalletsService walletsService(WalletRepository walletRepository, TransactionService transactionService) {
        return new WalletsService(transactionService, walletRepository);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository) {
        return new TransactionService(transactionRepository);
    }


    @Bean
    public PixService pixService(EventPixRepository eventPixRepository,
                                 WalletsService walletsService,
                                 TransactionRepository transactionRepository
    ) {
        return new PixService(eventPixRepository, walletsService, transactionRepository);
    }

    @Bean
    public WalletController walletController(WalletsService walletsService, TransactionService transactionService) {
        return new WalletController(walletsService, transactionService);
    }


    @Bean
    public PixController pixController(PixService pixService) {
        return new PixController(pixService);
    }
}
