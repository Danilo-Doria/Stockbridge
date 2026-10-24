CREATE TABLE inventory_movements
(
    id           BIGSERIAL PRIMARY KEY,
    product_id   BIGINT      NOT NULL REFERENCES products (id),
    request_id   BIGINT REFERENCES requests (id),
    performed_by BIGINT      NOT NULL REFERENCES users (id),
    type         VARCHAR(10) NOT NULL CHECK (type IN ('ENTRADA', 'SALIDA')),
    quantity     INTEGER     NOT NULL CHECK (quantity > 0),
    created_at   TIMESTAMP   NOT NULL
);
CREATE INDEX idx_movements_product ON inventory_movements (product_id);