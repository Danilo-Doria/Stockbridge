CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('EMPLEADO', 'BODEGA', 'ADMIN')),
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);