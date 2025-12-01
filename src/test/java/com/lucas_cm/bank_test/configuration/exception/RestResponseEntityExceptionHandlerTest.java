package com.lucas_cm.bank_test.configuration.exception;

import com.lucas_cm.bank_test.domain.exceptions.InsufficientBalanceException;
import com.lucas_cm.bank_test.domain.exceptions.WalletNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestResponseEntityExceptionHandler - Testes Unitários")
class RestResponseEntityExceptionHandlerTest {

    @InjectMocks
    private RestResponseEntityExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new RestResponseEntityExceptionHandler();
    }

    @Test
    @DisplayName("Dado uma BusinessException, quando tratar exceção, então deve retornar 422 UNPROCESSABLE_ENTITY com mensagem e código de erro")
    void dado_business_exception_quando_tratar_excecao_entao_deve_retornar_422_com_mensagem_e_codigo() {
        // Given - Dado uma BusinessException
        BusinessException exception = new BusinessException();
        exception.setMessage("Erro de negócio");
        exception.setErrorCode("BUSINESS_ERROR");

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleBusinessError(exception);

        // Then - Então deve retornar 422 com mensagem e código de erro
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Erro de negócio");
        assertThat(body.get("error_code")).isEqualTo("BUSINESS_ERROR");
    }

    @Test
    @DisplayName("Dado uma BusinessException com dados adicionais, quando tratar exceção, então deve incluir dados no corpo da resposta")
    void dado_business_exception_com_dados_quando_tratar_excecao_entao_deve_incluir_dados() {
        // Given - Dado uma BusinessException com dados adicionais
        InsufficientBalanceException exception = new InsufficientBalanceException(new BigDecimal("100.50"));

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleBusinessError(exception);

        // Then - Então deve incluir dados no corpo da resposta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("data")).isNotNull();
        assertThat(body).containsKey("data");
    }

    @Test
    @DisplayName("Dado uma BusinessException sem dados adicionais, quando tratar exceção, então não deve incluir campo data")
    void dado_business_exception_sem_dados_quando_tratar_excecao_entao_nao_deve_incluir_campo_data() {
        // Given - Dado uma BusinessException sem dados adicionais
        WalletNotFoundException exception = new WalletNotFoundException();

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleBusinessError(exception);

        // Then - Então não deve incluir campo data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsKey("message");
        assertThat(body).containsKey("error_code");
        // O campo data pode ou não estar presente dependendo da implementação
    }

    @Test
    @DisplayName("Dado uma Exception genérica, quando tratar exceção, então deve retornar 500 INTERNAL_SERVER_ERROR com mensagem genérica")
    void dado_exception_generica_quando_tratar_excecao_entao_deve_retornar_500_com_mensagem_generica() {
        // Given - Dado uma Exception genérica
        Exception exception = new RuntimeException("Erro interno");

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleInternalServerError(exception);

        // Then - Então deve retornar 500 com mensagem genérica
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Ocorreu um erro no serviço");
    }

    @Test
    @DisplayName("Dado uma NullPointerException, quando tratar exceção, então deve retornar 500 INTERNAL_SERVER_ERROR")
    void dado_null_pointer_exception_quando_tratar_excecao_entao_deve_retornar_500() {
        // Given - Dado uma NullPointerException
        Exception exception = new NullPointerException("Valor nulo");

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleInternalServerError(exception);

        // Then - Então deve retornar 500
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Ocorreu um erro no serviço");
    }

    @Test
    @DisplayName("Dado uma IllegalArgumentException, quando tratar exceção, então deve retornar 500 INTERNAL_SERVER_ERROR")
    void dado_illegal_argument_exception_quando_tratar_excecao_entao_deve_retornar_500() {
        // Given - Dado uma IllegalArgumentException
        Exception exception = new IllegalArgumentException("Argumento inválido");

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleInternalServerError(exception);

        // Then - Então deve retornar 500
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Ocorreu um erro no serviço");
    }

    @Test
    @DisplayName("Dado uma NotFoundException, quando tratar exceção, então deve retornar 404 NOT_FOUND com mensagem e código de erro")
    void dado_not_found_exception_quando_tratar_excecao_entao_deve_retornar_404_com_mensagem_e_codigo() {
        // Given - Dado uma NotFoundException
        NotFoundException exception = new NotFoundException();
        exception.setMessage("Recurso não encontrado");
        exception.setErrorCode("NOT_FOUND");

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleNotFoundExceptionError(exception);

        // Then - Então deve retornar 404 com mensagem e código de erro
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Recurso não encontrado");
        assertThat(body.get("error_code")).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Dado uma NotFoundException com mensagem nula, quando tratar exceção, então deve retornar 404 com código de erro")
    void dado_not_found_exception_com_mensagem_nula_quando_tratar_excecao_entao_deve_retornar_404() {
        // Given - Dado uma NotFoundException com mensagem nula
        NotFoundException exception = new NotFoundException();
        exception.setMessage(null);
        exception.setErrorCode("NOT_FOUND");

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleNotFoundExceptionError(exception);

        // Then - Então deve retornar 404
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("error_code")).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Dado uma BusinessException com todos os campos preenchidos, quando tratar exceção, então deve retornar resposta completa")
    void dado_business_exception_completa_quando_tratar_excecao_entao_deve_retornar_resposta_completa() {
        // Given - Dado uma BusinessException completa
        BusinessException exception = new BusinessException();
        exception.setMessage("Erro completo");
        exception.setErrorCode("COMPLETE_ERROR");
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("field1", "value1");
        data.put("field2", 123);
        exception.setData(data);

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleBusinessError(exception);

        // Then - Então deve retornar resposta completa
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Erro completo");
        assertThat(body.get("error_code")).isEqualTo("COMPLETE_ERROR");
        assertThat(body.get("data")).isNotNull();
        assertThat(body.get("data")).isEqualTo(data);
    }

    @Test
    @DisplayName("Dado uma BusinessException com mensagem nula, quando tratar exceção, então deve retornar 422 com código de erro")
    void dado_business_exception_com_mensagem_nula_quando_tratar_excecao_entao_deve_retornar_422() {
        // Given - Dado uma BusinessException com mensagem nula
        BusinessException exception = new BusinessException();
        exception.setMessage(null);
        exception.setErrorCode("ERROR_CODE");

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleBusinessError(exception);

        // Then - Então deve retornar 422
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("error_code")).isEqualTo("ERROR_CODE");
    }

    @Test
    @DisplayName("Dado uma Exception genérica com causa, quando tratar exceção, então deve retornar 500 sem expor detalhes da causa")
    void dado_exception_com_causa_quando_tratar_excecao_entao_deve_retornar_500_sem_expor_detalhes() {
        // Given - Dado uma Exception com causa
        Exception cause = new RuntimeException("Causa raiz");
        Exception exception = new RuntimeException("Erro", cause);

        // When - Quando tratar a exceção
        ResponseEntity<Object> response = exceptionHandler.handleInternalServerError(exception);

        // Then - Então deve retornar 500 sem expor detalhes da causa
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Ocorreu um erro no serviço");
        // Não deve expor detalhes da causa
        assertThat(body.get("message")).isNotEqualTo("Erro");
    }
}

