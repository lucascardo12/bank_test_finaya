package com.lucas_cm.bank_test.configuration;

import com.lucas_cm.bank_test.domain.repositories.EventPixRepository;
import com.lucas_cm.bank_test.domain.repositories.TransactionRepository;
import com.lucas_cm.bank_test.domain.repositories.WalletRepository;
import com.lucas_cm.bank_test.domain.services.PixService;
import com.lucas_cm.bank_test.domain.services.TransactionService;
import com.lucas_cm.bank_test.domain.services.WalletsService;
import com.lucas_cm.bank_test.infrastructure.controllers.PixController;
import com.lucas_cm.bank_test.infrastructure.controllers.WalletController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigurationBeans - Testes de Configuração")
class ConfigurationBeansTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EventPixRepository eventPixRepository;

    private ConfigurationBeans configurationBeans;

    @Test
    @DisplayName("Dado repositórios válidos, quando criar WalletsService, então deve retornar instância correta")
    void dado_repositorios_validos_quando_criar_wallets_service_entao_deve_retornar_instancia_correta() {
        // Given - Dado repositórios válidos
        configurationBeans = new ConfigurationBeans();
        TransactionService transactionService = new TransactionService(transactionRepository);

        // When - Quando criar WalletsService
        WalletsService result = configurationBeans.walletsService(walletRepository, transactionService);

        // Then - Então deve retornar instância correta
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(WalletsService.class);
    }

    @Test
    @DisplayName("Dado TransactionRepository válido, quando criar TransactionService, então deve retornar instância correta")
    void dado_transaction_repository_valido_quando_criar_transaction_service_entao_deve_retornar_instancia_correta() {
        // Given - Dado TransactionRepository válido
        configurationBeans = new ConfigurationBeans();

        // When - Quando criar TransactionService
        TransactionService result = configurationBeans.transactionService(transactionRepository);

        // Then - Então deve retornar instância correta
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(TransactionService.class);
    }

    @Test
    @DisplayName("Dado dependências válidas, quando criar PixService, então deve retornar instância correta")
    void dado_dependencias_validas_quando_criar_pix_service_entao_deve_retornar_instancia_correta() {
        // Given - Dado dependências válidas
        configurationBeans = new ConfigurationBeans();
        TransactionService transactionService = new TransactionService(transactionRepository);
        WalletsService walletsService = new WalletsService(transactionService, walletRepository);

        // When - Quando criar PixService
        PixService result = configurationBeans.pixService(
                eventPixRepository,
                walletsService,
                transactionRepository
        );

        // Then - Então deve retornar instância correta
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PixService.class);
    }

    @Test
    @DisplayName("Dado serviços válidos, quando criar WalletController, então deve retornar instância correta")
    void dado_servicos_validos_quando_criar_wallet_controller_entao_deve_retornar_instancia_correta() {
        // Given - Dado serviços válidos
        configurationBeans = new ConfigurationBeans();
        TransactionService transactionService = new TransactionService(transactionRepository);
        WalletsService walletsService = new WalletsService(transactionService, walletRepository);

        // When - Quando criar WalletController
        WalletController result = configurationBeans.walletController(walletsService, transactionService);

        // Then - Então deve retornar instância correta
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(WalletController.class);
    }

    @Test
    @DisplayName("Dado PixService válido, quando criar PixController, então deve retornar instância correta")
    void dado_pix_service_valido_quando_criar_pix_controller_entao_deve_retornar_instancia_correta() {
        // Given - Dado PixService válido
        configurationBeans = new ConfigurationBeans();
        TransactionService transactionService = new TransactionService(transactionRepository);
        WalletsService walletsService = new WalletsService(transactionService, walletRepository);
        PixService pixService = new PixService(eventPixRepository, walletsService, transactionRepository);

        // When - Quando criar PixController
        PixController result = configurationBeans.pixController(pixService);

        // Then - Então deve retornar instância correta
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PixController.class);
    }

    @Test
    @DisplayName("Dado a configuração completa, quando criar todos os beans, então deve criar hierarquia correta de dependências")
    void dado_configuracao_completa_quando_criar_todos_beans_entao_deve_criar_hierarquia_correta() {
        // Given - Dado a configuração completa
        configurationBeans = new ConfigurationBeans();

        // When - Quando criar todos os beans na ordem correta
        TransactionService transactionService = configurationBeans.transactionService(transactionRepository);
        WalletsService walletsService = configurationBeans.walletsService(walletRepository, transactionService);
        PixService pixService = configurationBeans.pixService(
                eventPixRepository,
                walletsService,
                transactionRepository
        );
        WalletController walletController = configurationBeans.walletController(walletsService, transactionService);
        PixController pixController = configurationBeans.pixController(pixService);

        // Then - Então deve criar hierarquia correta de dependências
        assertThat(transactionService).isNotNull();
        assertThat(walletsService).isNotNull();
        assertThat(pixService).isNotNull();
        assertThat(walletController).isNotNull();
        assertThat(pixController).isNotNull();
    }

    @Test
    @DisplayName("Dado ConfigurationBeans, quando verificar anotação, então deve estar anotado com @Configuration")
    void dado_configuration_beans_quando_verificar_anotacao_entao_deve_estar_anotado_com_configuration() {
        // Given - Dado ConfigurationBeans
        configurationBeans = new ConfigurationBeans();

        // When - Quando verificar anotação
        boolean hasConfigurationAnnotation = configurationBeans.getClass()
                .isAnnotationPresent(org.springframework.context.annotation.Configuration.class);

        // Then - Então deve estar anotado com @Configuration
        assertThat(hasConfigurationAnnotation).isTrue();
    }
}

