# Guia de Testes - Idempotência e Concorrência

Esta collection do Postman contém testes automatizados para validar a idempotência e concorrência do sistema PIX.

## 📋 Pré-requisitos

1. **Importar a Collection:**
   - Abra o Postman
   - Clique em **Import**
   - Selecione o arquivo `test_idempotency_and_concurrency.postman_collection.json`

2. **Configurar Environment:**
   - Use o environment `dev` existente ou crie um novo
   - Configure a variável `url` com o endereço da API:
     - **Local:** `http://localhost:8080`
     - **Outros ambientes:** ajuste conforme necessário

3. **Executar Setup:**
   - Execute primeiro a pasta **"Setup - Criar Wallets"** para criar as carteiras necessárias

## 🧪 Testes Disponíveis

### 1. **Test Idempotency**

Testa se requisições duplicadas com o mesmo `Idempotency-Key` não causam débitos duplicados.

#### **Transferência PIX - Primeira Chamada**
- Gera um `Idempotency-Key` único
- Cria uma transferência PIX
- Salva o `endToEndId` retornado
- Registra o saldo antes da duplicata

#### **Transferência PIX - Chamada Duplicada**
- Reutiliza o mesmo `Idempotency-Key` da primeira chamada
- **Validações:**
  - ✅ Deve retornar o mesmo `endToEndId`
  - ✅ O saldo não deve ter mudado (idempotência funcionando)
  - ✅ Não deve criar nova transação

**Resultado Esperado:**
- Mesmo `endToEndId` retornado
- Saldo permanece inalterado
- Console mostra: `✅ Idempotência funcionando: saldo não mudou`

---

### 2. **Test Concurrency**

Testa múltiplas transferências simultâneas para verificar se os locks pessimistas previnem race conditions.

#### **Preparar Teste de Concorrência**
- Gera 5 `Idempotency-Keys` únicos
- Registra o saldo inicial

#### **Transferências Concorrentes - Request 1, 2, 3**
- Executa 3 transferências PIX simultâneas (ou sequenciais)
- Cada uma com `Idempotency-Key` diferente
- Cada uma transfere R$ 50,00

#### **Verificar Resultado da Concorrência**
- **Validações:**
  - ✅ Saldo final deve ser: `saldo_inicial - (50.00 * 3)`
  - ✅ Todos os `endToEndIds` devem ser diferentes
  - ✅ Não deve haver débitos duplicados

**Resultado Esperado:**
- Saldo correto após 3 transferências
- Console mostra: `✅ Concorrência OK: saldo correto após múltiplas transferências`

---

### 3. **Test Concurrency - Same Wallet**

Testa saques concorrentes na mesma carteira para validar locks pessimistas.

#### **Preparar Teste de Concorrência na Mesma Wallet**
- Garante saldo suficiente (depósito de R$ 500,00)
- Registra saldo inicial

#### **Saque Concorrente 1 e 2**
- Executa 2 saques simultâneos de R$ 100,00 cada
- Ambos na mesma carteira

#### **Verificar Saldo Após Saques Concorrentes**
- **Validações:**
  - ✅ Saldo final deve ser: `saldo_inicial - 200.00`
  - ✅ Não deve permitir saldo negativo
  - ✅ Locks devem prevenir race conditions

**Resultado Esperado:**
- Saldo correto após 2 saques
- Console mostra: `✅ Concorrência OK: saldo correto após saques concorrentes`

---

## 🚀 Como Executar

### Opção 1: Executar Manualmente

1. Execute a pasta **"Setup - Criar Wallets"** primeiro
2. Execute **"Test Idempotency"** para testar idempotência
3. Execute **"Test Concurrency"** para testar concorrência
4. Execute **"Test Concurrency - Same Wallet"** para testar saques concorrentes

### Opção 2: Executar Collection Runner

1. Clique com botão direito na collection
2. Selecione **"Run collection"**
3. Configure:
   - **Iterations:** 1
   - **Delay:** 0ms (para testar concorrência real)
4. Clique em **"Run Test Idempotency and Concurrency"**

### Opção 3: Executar com Newman (CLI)

```bash
# Instalar Newman
npm install -g newman

# Executar collection
newman run postman/test_idempotency_and_concurrency.postman_collection.json \
  -e postman/dev.postman_environment.json \
  --delay-request 0
```

---

## 📊 Interpretando os Resultados

### ✅ Teste Passou
- Console mostra mensagens de sucesso
- Saldos calculados corretamente
- `endToEndIds` únicos quando esperado
- Mesmo `endToEndId` em requisições idempotentes

### ❌ Teste Falhou
- Console mostra mensagens de erro
- Saldo incorreto (possível race condition)
- `endToEndIds` duplicados quando não deveriam
- Saldo negativo (locks não funcionando)

---

## 🔍 O que os Testes Validam

### Idempotência
- ✅ Requisições duplicadas retornam mesmo resultado
- ✅ Não criam transações duplicadas
- ✅ Não alteram saldo em requisições duplicadas

### Concorrência
- ✅ Locks pessimistas previnem race conditions
- ✅ Múltiplas transferências simultâneas processam corretamente
- ✅ Saques concorrentes não causam saldo negativo
- ✅ Saldos calculados corretamente após operações concorrentes

---

## ⚠️ Notas Importantes

1. **Ordem de Execução:**
   - Sempre execute o **Setup** primeiro
   - Os testes dependem das carteiras criadas no setup

2. **Variáveis de Ambiente:**
   - As variáveis são criadas automaticamente pelos scripts
   - Verifique o console do Postman para ver os valores

3. **Para Teste Real de Concorrência:**
   - Use o Collection Runner com delay 0ms
   - Ou execute múltiplas requisições simultaneamente via script externo

4. **Limpeza:**
   - Após os testes, você pode deletar as carteiras criadas manualmente
   - Ou criar novos testes de limpeza

---

## 🐛 Troubleshooting

### Erro: "Carteira não encontrada"
- Execute o **Setup** primeiro
- Verifique se as variáveis `wallet_from_id` e `wallet_to_id` foram criadas

### Erro: "Saldo insuficiente"
- O setup faz depósito de R$ 1000,00
- Se necessário, ajuste os valores nos testes

### Erro: "Chave PIX não encontrada"
- Verifique se a chave PIX foi registrada no setup
- A variável `wallet_to_pix_key` deve estar configurada

---

## 📝 Exemplo de Saída do Console

```
✅ Idempotência funcionando: saldo não mudou
✅ Concorrência OK: saldo correto após múltiplas transferências
✅ Todos os endToEndIds são únicos
✅ Concorrência OK: saldo correto após saques concorrentes
```

