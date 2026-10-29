CREATE TABLE directivas (
    directiva_id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL,
    cargo VARCHAR(255) NOT NULL,
    periodo_inicio DATE NOT NULL,
    periodo_fin DATE,
    estado VARCHAR(255) NOT NULL,

    CONSTRAINT fk_directiva_socio
        FOREIGN KEY (socio_id) REFERENCES socios (socio_id)
);
