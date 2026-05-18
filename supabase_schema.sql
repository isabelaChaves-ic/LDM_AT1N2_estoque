-- ============================================================
-- Schema do Supabase — Estocadão
-- Executar no SQL Editor do painel do Supabase
-- ============================================================

-- Tabela de produtos
CREATE TABLE IF NOT EXISTS products (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar     NOT NULL,
    description text,
    sku         varchar     NOT NULL UNIQUE,
    category    varchar,
    created_at  timestamp   NOT NULL DEFAULT now(),
    updated_at  timestamp   NOT NULL DEFAULT now()
);

-- Tabela de itens de estoque
CREATE TABLE IF NOT EXISTS stock_items (
    id          uuid            PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  uuid            NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity    integer         NOT NULL DEFAULT 0,
    unit_price  decimal(10,2)   NOT NULL,
    location    varchar,
    updated_at  timestamp       NOT NULL DEFAULT now()
);

-- Índice para buscas por produto
CREATE INDEX IF NOT EXISTS idx_stock_items_product_id ON stock_items(product_id);

-- View para o endpoint GET /stock/summary
-- Agrega quantidade total por produto via GROUP BY + SUM (conforme requisito)
CREATE OR REPLACE VIEW stock_summary AS
    SELECT
        p.id   AS product_id,
        p.name AS product_name,
        COALESCE(SUM(s.quantity), 0)::integer AS total_quantity
    FROM products p
    LEFT JOIN stock_items s ON s.product_id = p.id
    GROUP BY p.id, p.name;
