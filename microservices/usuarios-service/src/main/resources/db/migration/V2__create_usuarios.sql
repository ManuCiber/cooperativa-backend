CREATE TABLE usuarios
(
    usuario_id     BIGINT                         NOT NULL,
    empleado_id    BIGINT                         NOT NULL,
    nombre_usuario VARCHAR(255),
    password       VARCHAR(255),
    rol_usuario    VARCHAR(255),
    estado_usuario VARCHAR(255)                   NOT NULL,
    created_at     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (usuario_id)
);

ALTER TABLE usuarios
    ADD CONSTRAINT uc_usuarios_empleado UNIQUE (empleado_id);

ALTER TABLE usuarios
    ADD CONSTRAINT FK_USUARIOS_ON_EMPLEADO FOREIGN KEY (empleado_id) REFERENCES empleados (empleado_id);