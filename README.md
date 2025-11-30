# Projeto PIX Wallet

Este projeto simula um sistema simplificado de carteiras digitais com
suporte a: - Transferências via PIX - Webhook de confirmação do arranjo
PIX - Controle de idempotência - Histórico de saldo por data/hora -
Operações de depósito e saque

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=lucascardo12_bank_test_finaya&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=lucascardo12_bank_test_finaya)
------------------------------------------------------------------------

## 🚀 **Funcionalidades Principais**

### **1. Criar Carteira**

`POST /wallets`\
Cria uma nova carteira no sistema.

------------------------------------------------------------------------

### **2. Consultar Saldo Histórico**

`GET /wallets/{id}/balance?at=2025-10-09T15:00:00Z`\
Retorna o saldo da carteira em um instante específico do tempo.

Aceita `LocalDateTime` como parâmetro.

------------------------------------------------------------------------

### **3. Transferência PIX**

`POST /pix/transfers`\
Headers:\
`Idempotency-Key: <uuid>`

Body:

``` json
{
  "fromWalletId": "string",
  "toPixKey": "string",
  "amount": 100.00
}
```

Resposta:

``` json
{
  "endToEndId": "E2E123",
  "status": "PENDING"
}
```

------------------------------------------------------------------------

### **4. Webhook do Arranjo PIX**

`POST /pix/webhook`\
Idempotente por `eventId`.

Body:

``` json
{
  "endToEndId": "string",
  "eventId": "string",
  "eventType": "RECEIVED | CONFIRMED",
  "occurredAt": "2025-01-01T10:00:00"
}
```

Esse endpoint simula a confirmação do Banco Central no fluxo PIX.

------------------------------------------------------------------------

## 🧱 **Entidades Criadas**

### **Wallet**

Representa uma carteira digital.

### **Transaction**

Registra operações: - `DEPOSIT` - `WITHDRAW` - `PIX_SENT` -
`PIX_RECEIVED`

### **PixTransfer**

Representa uma transferência PIX iniciada.

### **PixWebhookEvent**

Registra notificações PIX recebidas.

------------------------------------------------------------------------

## ⚠️ Exceptions Personalizadas

### `WalletNotFoundException`

Dispara quando uma carteira não é encontrada.

### `PixTransferNotFoundException`

Padrão:

    "PIX_TRANSFER_NOT_FOUND"

------------------------------------------------------------------------

## ▶️ Fluxo PIX (simplificado)

1. Usuário inicia transferência PIX
2. Sistema cria `PixTransfer` com status `PENDING`
3. Arranjo PIX envia webhook
4. Sistema confirma transferência e atualiza saldos
5. Nova `Transaction` é criada nas carteiras envolvidas

------------------------------------------------------------------------

## 🛠️ Tecnologias

- Java 17+
- Spring Boot
- Spring Data JPA
- H2/PostgreSQL
- REST API
- Idempotency via Redis ou banco relacional

------------------------------------------------------------------------

## 📦 Como Rodar

### 1. Clonar o repositório

    git clone <seu-repo>

### 2. Rodar com Maven

    mvn spring-boot:run

------------------------------------------------------------------------

## 📄 Licença

MIT License.
