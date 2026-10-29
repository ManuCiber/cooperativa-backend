CREATE TABLE socios (
                        socio_id BIGSERIAL PRIMARY KEY,

                        no_socio VARCHAR(20) NOT NULL UNIQUE,

                        nombre VARCHAR(100) NOT NULL,
                        apellidos VARCHAR(100) NOT NULL,

                        dni_cedula VARCHAR(20) NOT NULL UNIQUE,

                        fecha_nacimiento DATE,

                        direccion VARCHAR(255),
                        telefono VARCHAR(20),

                        email VARCHAR(150) UNIQUE,

                        fecha_ingreso DATE NOT NULL DEFAULT CURRENT_DATE,

                        estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

                        tipo_socio VARCHAR(30) NOT NULL,

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT chk_socio_estado
                            CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO'))
);