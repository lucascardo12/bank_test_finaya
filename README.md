# Projeto PIX Wallet

Este projeto simula um sistema simplificado de carteiras digitais com suporte a:
- Transferências via PIX
- Webhook de confirmação do arranjo PIX
- Controle de idempotência
- Histórico de saldo por data/hora
- Operações de depósito e saque

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=lucascardo12_bank_test_finaya)](https://sonarcloud.io/summary/new_code?id=lucascardo12_bank_test_finaya)

---

## 📋 Índice

- [Funcionalidades Principais](#-funcionalidades-principais)
- [Instalação e Execução](#-instalação-e-execução)
- [Testes](#-testes)
- [Swagger/OpenAPI](#-swaggeropenapi)
- [Decisões de Design](#-decisões-de-design)
- [Trade-offs e Compromissos](#-trade-offs-e-compromissos)
- [Tecnologias](#-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Licença](#-licença)

---

## 🚀 Funcionalidades Principais

### **1. Criar Carteira**

`POST /wallets`

Cria uma nova carteira no sistema.

**Body:**
```json
{
  "userId": "user123"
}
```

---

### **2. Consultar Saldo Histórico**

`GET /wallets/{id}/balance?at=2025-10-09T15:00:00Z`

Retorna o saldo da carteira em um instante específico do tempo. Aceita `LocalDateTime` como parâmetro no formato ISO-8601.

**Exemplo sem parâmetro (saldo atual):**
```
GET /wallets/{id}/balance
```

**Exemplo com data/hora:**
```
GET /wallets/{id}/balance?at=2025-10-09T15:00:00Z
```

---

### **3. Transferência PIX**

`POST /pix/transfers`

**Headers:**
```
Idempotency-Key: <uuid>
```

**Body:**
```json
{
  "fromWalletId": "string",
  "toPixKey": "string",
  "amount": 100.00
}
```

**Resposta:**
```json
{
  "endToEndId": "E2E123",
  "status": "PENDING"
}
```

---

### **4. Webhook do Arranjo PIX**

`POST /pix/webhook`

Idempotente por `eventId`. Simula a confirmação do Banco Central no fluxo PIX.

**Body:**
```json
{
  "endToEndId": "string",
  "eventId": "string",
  "eventType": "RECEIVED | CONFIRMED",
  "occurredAt": "2025-01-01T10:00:00Z"
}
```

---

### **5. Depósito e Saque**

`POST /wallets/{id}/deposit`
`POST /wallets/{id}/withdraw`

**Body:**
```json
{
  "amount": 100.00
}
```

---

### **6. Registrar Chave PIX**

`POST /wallets/{id}/pix-keys`

**Body:**
```json
{
  "key": "chave-pix-exemplo"
}
```

---

## 📦 Instalação e Execução

### Pré-requisitos

- **Java 21** ou superior
- **Gradle 8.x** (ou use o wrapper incluído: `./gradlew`)
- **PostgreSQL 15+** (ou use Docker Compose)

### Opção 1: Usando Docker Compose (Recomendado)

1. **Clone o repositório:**
   ```bash
   git clone <seu-repo>
   cd bank_test_finaya
   ```

2. **Inicie o PostgreSQL com Docker Compose:**
   ```bash
   docker-compose up -d
   ```

3. **Configure as variáveis de ambiente:**
   
   Crie um arquivo `.env` na raiz do projeto ou exporte as variáveis:
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/bank_test
   export DB_USER=postgres
   export DB_PASSWORD=postgres
   export LOG_LEVEL=INFO
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/bank_test"
   $env:DB_USER="postgres"
   $env:DB_PASSWORD="postgres"
   $env:LOG_LEVEL="INFO"
   ```

4. **Execute a aplicação:**
   ```bash
   ./gradlew bootRun
   ```
   
   **Windows:**
   ```cmd
   gradlew.bat bootRun
   ```

5. **Acesse a aplicação:**
   - API: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

### Opção 2: PostgreSQL Local

1. **Instale e configure o PostgreSQL localmente**

2. **Crie o banco de dados:**
   ```sql
   CREATE DATABASE bank_test;
   ```

3. **Configure as variáveis de ambiente** (mesmo processo da Opção 1)

4. **Execute a aplicação:**
   ```bash
   ./gradlew bootRun
   ```

### Opção 3: Build e Executar JAR

1. **Build do projeto:**
   ```bash
   ./gradlew build
   ```

2. **Execute o JAR:**
   ```bash
   java -jar build/libs/bank-test-0.0.1-SNAPSHOT.jar
   ```

---

## 🧪 Testes

### Executar todos os testes

```bash
./gradlew test
```

**Windows:**
```cmd
gradlew.bat test
```

### Executar testes com relatório

```bash
./gradlew test --info
```

Os relatórios de teste estarão disponíveis em: `build/reports/tests/test/index.html`

### Cobertura de testes

O projeto utiliza JUnit 5 para testes unitários e de integração, cobrindo:
- Controllers (PixController, WalletController)
- Services (PixService, WalletsService, TransactionService)
- Exceptions personalizadas
- Entidades e enums

---

## 📚 Swagger/OpenAPI

A documentação da API está disponível através do **SpringDoc OpenAPI**.

### Acessar Swagger UI

Após iniciar a aplicação, acesse:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML:** `http://localhost:8080/v3/api-docs.yaml`

### Funcionalidades do Swagger

- **Documentação interativa** de todos os endpoints
- **Teste de requisições** diretamente pela interface
- **Esquemas de dados** (DTOs) documentados
- **Códigos de resposta** e exemplos
- **Tags organizadas** por funcionalidade (Pix, Wallets)

### Exemplo de uso

1. Acesse `http://localhost:8080/swagger-ui.html`
2. Expanda o endpoint desejado (ex: `POST /pix/transfers`)
3. Clique em "Try it out"
4. Preencha os parâmetros e body
5. Execute a requisição
6. Veja a resposta em tempo real

---

## 🏗️ Decisões de Design

### Arquitetura em Camadas (Layered Architecture)

O projeto segue uma arquitetura em camadas bem definida, separando responsabilidades:

#### **1. Camada de Domínio (`domain/`)**
- **Entidades:** Representam o modelo de negócio (WalletEntity, TransactionEntity, EventPixEntity)
- **Repositórios:** Interfaces que abstraem o acesso a dados
- **Serviços:** Lógica de negócio pura, independente de frameworks
- **Exceções:** Exceções de domínio específicas do negócio

**Benefícios:**
- ✅ Testabilidade: Lógica de negócio pode ser testada sem dependências externas
- ✅ Manutenibilidade: Mudanças na infraestrutura não afetam o domínio
- ✅ Reutilização: Serviços podem ser reutilizados em diferentes contextos

#### **2. Camada de Infraestrutura (`infrastructure/`)**
- **Controllers:** Endpoints REST, validação de entrada
- **DTOs:** Objetos de transferência de dados, isolam a API do domínio

**Benefícios:**
- ✅ Desacoplamento: Mudanças na API não afetam o domínio
- ✅ Versionamento: Facilita evolução da API sem quebrar contratos

#### **3. Camada de Configuração (`configuration/`)**
- **ConfigurationBeans:** Configuração explícita de dependências
- **Exception Handlers:** Tratamento centralizado de exceções

**Benefícios:**
- ✅ Controle: Dependências explícitas facilitam testes e manutenção
- ✅ Consistência: Respostas de erro padronizadas

### Atendimento aos Requisitos Funcionais

#### **RF1: Transferências PIX**
- ✅ Implementado via `PixService.transfer()`
- ✅ Validação de saldo antes da transferência
- ✅ Criação de transações de débito e crédito
- ✅ Status inicial `PENDING` aguardando confirmação

#### **RF2: Webhook de Confirmação**
- ✅ Implementado via `PixService.processWebhook()`
- ✅ Idempotência garantida por `eventId` único
- ✅ Atualização de saldos apenas quando `CONFIRMED`
- ✅ Suporte a eventos `RECEIVED` e `CONFIRMED`

#### **RF3: Controle de Idempotência**
- ✅ Transferências: Via header `Idempotency-Key`
- ✅ Webhooks: Via campo `eventId` único no banco
- ✅ Transações: Via `endToEndId` único

#### **RF4: Histórico de Saldo**
- ✅ Implementado via `TransactionService.amountByWalletIdAndDate()`
- ✅ Cálculo baseado em transações até a data especificada
- ✅ Suporte a consulta de saldo atual ou histórico

#### **RF5: Depósito e Saque**
- ✅ Implementado via `WalletsService.deposit()` e `withdraw()`
- ✅ Validação de saldo para saques
- ✅ Criação automática de transações

### Atendimento aos Requisitos Não-Funcionais

#### **RNF1: Performance**
- ✅ Uso de índices únicos no banco (`endToEndId`, `eventId`, `pixKey`)
- ✅ Transações otimizadas com `@Transactional`
- ✅ Queries diretas para cálculo de saldo histórico

#### **RNF2: Confiabilidade**
- ✅ Idempotência em operações críticas
- ✅ Validações de negócio (saldo insuficiente, carteira não encontrada)
- ✅ Tratamento centralizado de exceções

#### **RNF3: Manutenibilidade**
- ✅ Separação clara de responsabilidades
- ✅ Código testável e coberto por testes
- ✅ Uso de Lombok para reduzir boilerplate

#### **RNF4: Escalabilidade**
- ✅ Arquitetura preparada para evolução
- ✅ Repositórios podem ser substituídos (ex: Redis para cache)
- ✅ Serviços independentes facilitam distribuição futura

---

## ⚖️ Trade-offs e Compromissos

### 1. **Idempotência via Banco de Dados vs Redis**

**Decisão:** Implementação via banco de dados relacional (PostgreSQL)

**Motivo:**
- ✅ Simplicidade: Não requer infraestrutura adicional
- ✅ Consistência: Garantia ACID para operações críticas
- ✅ Persistência: Histórico completo de operações idempotentes

**Trade-off:**
- ⚠️ Performance: Redis seria mais rápido para leituras frequentes
- ⚠️ Escalabilidade: Em alta escala, Redis seria mais adequado

**Compromisso:** Para MVP/protótipo, banco relacional é suficiente. Em produção com alto volume, considerar Redis para cache de idempotência.

---

### 2. **Cálculo de Saldo Histórico: Soma de Transações vs Snapshot**

**Decisão:** Cálculo em tempo real somando transações até a data

**Motivo:**
- ✅ Precisão: Sempre reflete o estado real
- ✅ Simplicidade: Não requer tabela de snapshots
- ✅ Flexibilidade: Funciona para qualquer data/hora

**Trade-off:**
- ⚠️ Performance: Em sistemas com milhões de transações, pode ser lento
- ⚠️ Carga no banco: Query agregada pode ser custosa

**Compromisso:** Para volumes moderados, a solução atual é adequada. Em produção com alto volume, implementar snapshots periódicos ou materialized views.

---

### 3. **Transações Síncronas vs Assíncronas**

**Decisão:** Processamento síncrono de transferências PIX

**Motivo:**
- ✅ Simplicidade: Fluxo direto e fácil de debugar
- ✅ Consistência: Resposta imediata ao usuário
- ✅ Adequado para MVP

**Trade-off:**
- ⚠️ Latência: Usuário aguarda confirmação do webhook
- ⚠️ Escalabilidade: Em alta concorrência, pode sobrecarregar

**Compromisso:** Para o escopo atual, síncrono é suficiente. Em produção, considerar fila de mensagens (RabbitMQ/Kafka) para processamento assíncrono.

---

### 4. **Validação de Saldo: Otimista vs Pessimista**

**Decisão:** Validação otimista (check-then-act)

**Motivo:**
- ✅ Performance: Não bloqueia outras operações
- ✅ Simplicidade: Código mais direto

**Trade-off:**
- ⚠️ Race conditions: Em alta concorrência, pode haver saldo negativo
- ⚠️ Consistência: Requer locks ou versionamento

**Compromisso:** Para MVP, aceitável. Em produção, implementar locks pessimistas ou versionamento otimista (optimistic locking) na entidade Wallet.

---

### 5. **Configuração Manual de Beans vs Auto-configuração**

**Decisão:** Configuração manual via `@Configuration` e `@Bean`

**Motivo:**
- ✅ Controle explícito de dependências
- ✅ Facilita testes unitários
- ✅ Documentação clara das dependências

**Trade-off:**
- ⚠️ Mais código: Requer classe de configuração
- ⚠️ Manutenção: Mudanças requerem atualização manual

**Compromisso:** Trade-off aceito pela clareza e testabilidade. Em projetos maiores, considerar injeção automática com `@Component` e `@Service`.

---

### 6. **H2 vs PostgreSQL para Desenvolvimento**

**Decisão:** PostgreSQL desde o início

**Motivo:**
- ✅ Consistência: Mesmo banco em dev e produção
- ✅ Features: Suporte completo a constraints e índices
- ✅ Realismo: Testa comportamento real do banco

**Trade-off:**
- ⚠️ Setup: Requer instalação/configuração do PostgreSQL
- ⚠️ Portabilidade: Mais difícil para desenvolvedores iniciantes

**Compromisso:** Docker Compose resolve o problema de setup. Para desenvolvedores que preferem simplicidade, poderia ter perfil H2 opcional.

---

## 🛠️ Tecnologias

- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.8** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL 15** - Banco de dados relacional
- **Gradle** - Gerenciador de dependências
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI 2.7.0** - Documentação da API (Swagger)
- **JUnit 5** - Framework de testes

---

## 🧱 Entidades Criadas

### **Wallet**
Representa uma carteira digital com saldo atual, chave PIX e usuário associado.

### **Transaction**
Registra todas as operações financeiras:
- `DEPOSIT` - Depósito na carteira
- `WITHDRAW` - Saque da carteira
- `PIX_TRANSFER_OUT` - Transferência PIX enviada
- `PIX_TRANSFER_IN` - Transferência PIX recebida

Status: `PENDING`, `CONFIRMED`, `REJECTED`

### **EventPix**
Registra notificações PIX recebidas do arranjo, garantindo idempotência via `eventId` único.

---

## ⚠️ Exceptions Personalizadas

- `WalletNotFoundException` - Carteira não encontrada
- `PixTransferNotFoundException` - Transferência PIX não encontrada
- `InsufficientBalanceException` - Saldo insuficiente
- `UserAlreadyHasWalletException` - Usuário já possui carteira
- `TransactionEndToEndIdAlreadyExistsException` - Transação duplicada
- `EventPixIdAlreadyExistsException` - Evento PIX duplicado

---

## ▶️ Fluxo PIX (simplificado)

1. Usuário inicia transferência PIX via `POST /pix/transfers`
2. Sistema valida saldo e cria transações `PENDING` (débito e crédito)
3. Sistema retorna `endToEndId` com status `PENDING`
4. Arranjo PIX envia webhook via `POST /pix/webhook`
5. Sistema processa webhook, atualiza status e saldos se `CONFIRMED`
6. Transações são atualizadas para `CONFIRMED` e saldos são ajustados

---

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/lucas_cm/bank_test/
│   │   ├── configuration/          # Configurações e exception handlers
│   │   ├── domain/                 # Camada de domínio
│   │   │   ├── entities/          # Entidades JPA
│   │   │   ├── exceptions/        # Exceções de domínio
│   │   │   ├── repositories/     # Interfaces de repositório
│   │   │   └── services/         # Lógica de negócio
│   │   └── infrastructure/        # Camada de infraestrutura
│   │       ├── controllers/      # Controllers REST
│   │       └── dtos/             # Data Transfer Objects
│   └── resources/
│       └── application.properties # Configurações da aplicação
└── test/                          # Testes unitários e de integração
```

---

## 📄 Licença

MIT License.
