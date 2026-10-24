CREATE TABLE requests
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    product_id  BIGINT      NOT NULL REFERENCES products (id),
    quantity    INTEGER     NOT NULL CHECK (quantity > 0),
    status      VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'ENTREGADA', 'CANCELADA')),
    observation VARCHAR(500),
    created_at  TIMESTAMP   NOT NULL
);
CREATE INDEX idx_requests_user ON requests (user_id);
CREATE INDEX idx_requests_product ON requests (product_id);