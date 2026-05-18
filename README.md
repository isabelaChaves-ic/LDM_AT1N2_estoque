# Estocadão — API de Controle de Estoque

API REST desenvolvida com **Kotlin Multiplatform + Ktor** para gerenciamento de estoque de produtos, com persistência no **Supabase (PostgreSQL)**.

---

## Requisitos

- JDK 17 ou superior
- Gradle 8.x (ou usar o wrapper `./gradlew`)
- Conta no [Supabase](https://supabase.com) com as tabelas configuradas

---

## Configuração do Banco de Dados (Supabase)

Antes de executar, crie as seguintes tabelas no seu projeto Supabase:

```sql
-- Tabela de produtos
CREATE TABLE products (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar        NOT NULL,
    description text,
    sku         varchar        NOT NULL UNIQUE,
    category    varchar,
    created_at  timestamp      DEFAULT now(),
    updated_at  timestamp      DEFAULT now()
);

-- Tabela de itens de estoque
CREATE TABLE stock_items (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  uuid           NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity    integer        NOT NULL DEFAULT 0,
    unit_price  decimal(10,2)  NOT NULL,
    location    varchar,
    updated_at  timestamp      DEFAULT now()
);

-- View para o endpoint /stock/summary (GROUP BY + SUM via SQL)
CREATE VIEW stock_summary AS
    SELECT
        p.id   AS product_id,
        p.name AS product_name,
        COALESCE(SUM(s.quantity), 0)::integer AS total_quantity
    FROM products p
    LEFT JOIN stock_items s ON s.product_id = p.id
    GROUP BY p.id, p.name;
```

---

## Variáveis de Ambiente

| Variável        | Descrição                              | Exemplo                                |
|-----------------|----------------------------------------|----------------------------------------|
| `SUPABASE_URL`  | URL do projeto no Supabase             | `https://xyzxyz.supabase.co`           |
| `SUPABASE_KEY`  | Chave anon/public do projeto Supabase  | `eyJhbGci...`                          |
| `PORT`          | Porta do servidor (opcional)           | `8080` (padrão)                        |

### Como configurar localmente

Crie ou edite o arquivo `local.properties` na raiz do projeto:

```properties
SUPABASE_URL=https://seu-projeto.supabase.co
SUPABASE_KEY=sua_chave_anon_aqui
```

> **⚠️ Nunca commite o arquivo `local.properties` com credenciais reais.** Ele já está no `.gitignore`.

Alternativamente, exporte as variáveis no terminal antes de executar:

```bash
export SUPABASE_URL=https://seu-projeto.supabase.co
export SUPABASE_KEY=sua_chave_anon_aqui
```

---

## Como Executar

### macOS / Linux

```bash
./gradlew :server:run
```

### Windows

```bat
.\gradlew.bat :server:run
```

O servidor sobe na porta `8080` por padrão: `http://localhost:8080`

---

## Endpoints da API

### Produtos — `/products`

| Método | Rota              | Descrição                    | Status de sucesso |
|--------|-------------------|------------------------------|-------------------|
| GET    | `/products`       | Lista todos os produtos      | 200               |
| GET    | `/products/{id}`  | Busca produto por ID         | 200 / 404         |
| POST   | `/products`       | Cadastra novo produto        | 201               |
| PUT    | `/products/{id}`  | Atualiza produto             | 200 / 404         |
| DELETE | `/products/{id}`  | Remove produto               | 204 / 404         |

### Estoque — `/stock`

| Método | Rota              | Descrição                       | Status de sucesso |
|--------|-------------------|---------------------------------|-------------------|
| GET    | `/stock`          | Lista todos os itens de estoque | 200               |
| GET    | `/stock/{id}`     | Busca item por ID               | 200 / 404         |
| POST   | `/stock`          | Adiciona item ao estoque        | 201               |
| PUT    | `/stock/{id}`     | Atualiza item do estoque        | 200 / 404         |
| DELETE | `/stock/{id}`     | Remove item do estoque          | 204 / 404         |
| GET    | `/stock/summary`  | Total em estoque por produto    | 200               |

### Exemplo de payload — Produto (POST /products)

```json
{
  "name": "Caneta Azul",
  "description": "Caneta esferográfica azul 0.7mm",
  "sku": "CAN-AZ-001",
  "category": "Papelaria"
}
```

### Exemplo de payload — Item de Estoque (POST /stock)

```json
{
  "product_id": "uuid-do-produto",
  "quantity": 100,
  "unit_price": 1.99,
  "location": "Prateleira A3"
}
```

### Exemplo de resposta — GET /stock/summary

```json
[
  {
    "product_id": "uuid-do-produto",
    "product_name": "Caneta Azul",
    "total_quantity": 350
  }
]
```

---

## Estrutura do Projeto

```
server/src/main/kotlin/com/example/estoque/
├── Application.kt                  # Entry point, configuração do Supabase
├── Constants.kt                    # Constantes da aplicação
├── models/
│   ├── Product.kt                  # Modelo de produto
│   ├── StockItem.kt                # Modelo de item de estoque
│   └── StockSummary.kt             # Modelo para o endpoint summary
└── plugins/
    ├── Routing.kt                  # Registro das rotas
    ├── Serialization.kt            # Configuração do kotlinx.serialization
    └── routes/
        ├── ProductRoutes.kt        # Rotas CRUD de produtos
        └── StockRoutes.kt          # Rotas CRUD de estoque + summary
```

---

## Tecnologias

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Ktor](https://ktor.io/) — framework HTTP server
- [Supabase Kotlin](https://github.com/supabase-community/supabase-kt) — cliente Supabase
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) — serialização JSON
