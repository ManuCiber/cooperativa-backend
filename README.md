# Cooperativa-MS

Plataforma de servicios para gestionar operaciones de una cooperativa. La solución está organizada como microservicios Java con Spring Boot, comunicación HTTP, descubrimiento de servicios con Eureka, configuración centralizada con Spring Cloud Config y persistencia PostgreSQL con migraciones Flyway.

## Servicios

| Componente | Responsabilidad | Puerto local | Base de datos / esquema PostgreSQL |
|---|---|---:|---|
| `api-gateway` | Entrada HTTP y enrutamiento hacia los servicios | 8080 | — |
| `config-server` | Publicación de configuración central | 8888 | — |
| `discovery-server` | Registro y descubrimiento de instancias (Eureka) | 8761 | — |
| `socio-service` | Socios y directivas | 8081 | Base `cooperativa_socios`, esquema `socios` |
| `cuentas-service` | Cuentas, transacciones, aportaciones y ahorros programados | 8082 | Base `cooperativa_cuentas`, esquema `cuentas` |
| `prestamo-service` | Préstamos, productos financieros, garantías y pagos | 8083 | Base `cooperativa_prestamos`, esquema `prestamos` |
| `usuarios-service` | Empleados y usuarios | 8084 | Base `cooperativa_usuarios`, esquema `usuarios` |
| `configuration-service` | Parámetros funcionales de la cooperativa y auditoría | 8085 | MongoDB `cooperativa_config` |
| `postgres` | Instancia PostgreSQL compartida; las bases son independientes | 5432 | Una base por microservicio |
| `mongodb` | Persistencia de configuración funcional | 27017 | Base `cooperativa_config` |

El gateway actualmente enruta `/api/socios` y `/api/directivas` a `socio-service`, `/api/cuentas`, `/api/aportaciones`, `/api/transacciones` y `/api/ahorros-programados` a `cuentas-service`, `/api/prestamos`, `/api/productos-financieros`, `/api/garantias` y `/api/pago-prestamo` a `prestamo-service`, `/api/empleados` a `usuarios-service` y `/api/v1/configuracion` a `configuration-service`. Los servicios también quedan disponibles directamente en sus puertos locales.

## Requisitos

- Docker Desktop o Docker Engine con Docker Compose v2.
- Para ejecutar los servicios sin contenedores: JDK 21 y Maven.

## Ejecución con Docker Compose

El archivo `docker-compose.yml` de la raíz define todos los servicios y sus dependencias junto con PostgreSQL y MongoDB en una red compartida. Para iniciar toda la plataforma, desde la raíz:

```bash
docker compose up --build
```

La primera ejecución compila las aplicaciones en imágenes Docker y puede tardar varios minutos. El contenedor PostgreSQL inicializa una base independiente por microservicio; Flyway crea el esquema y las tablas al iniciar cada servicio. Eureka está disponible en `http://localhost:8761` y el gateway en `http://localhost:8080`.

Las aplicaciones se pueden iniciar en conjunto con `docker compose up --build <servicio>` desde la raíz (por ejemplo, `docker compose up --build socio-service`); las dependencias compartidas declaradas en el Compose principal se iniciarán automáticamente. Para levantar o detener toda la plataforma, use los comandos desde la raíz.

Discovery Server define su puerto (`8761`) y su configuración Eureka localmente; no depende del Config Server para arrancar. En Docker Compose, Eureka tiene desactivada la autopreservación para que retire instancias cuyo lease vence y no mantenga instancias caídas como `UP`. Este ajuste es sólo para desarrollo; en producción debe mantenerse la autopreservación y resolverse la causa de los heartbeats perdidos (conectividad, pausas o capacidad del servidor). Si inicia Discovery Server directamente fuera de Compose, aplique `EUREKA_SERVER_ENABLE_SELF_PRESERVATION=false` sólo en ese entorno local.

Para probar manualmente los endpoints con datos de ejemplo, orden de ejecución y respuestas esperadas, consulte [`datos-prueba-microservicios.txt`](datos-prueba-microservicios.txt).

Para cambiar las credenciales locales de PostgreSQL, defina `POSTGRES_USER` y `POSTGRES_PASSWORD` antes de iniciar Compose. Los valores por defecto son sólo para desarrollo local; no deben usarse en producción. Los datos permanecen en el volumen `postgres-data`. Los scripts de inicialización sólo se ejecutan cuando PostgreSQL crea un volumen de datos vacío; un volumen ya existente requiere crear las nuevas bases manualmente.

`configuration-service` requiere `CONFIG_ADMIN_USERNAME` y `CONFIG_ADMIN_PASSWORD` para habilitar las operaciones de escritura; no hay credenciales predeterminadas. MongoDB se almacena en el volumen `mongodb-data`. La consulta `GET /api/v1/configuracion` es pública; la inicialización, las actualizaciones y el historial requieren HTTP Basic con el usuario administrador. En producción, sustituya este mecanismo de desarrollo por autenticación centralizada y gestione credenciales mediante un almacén de secretos.

Antes de iniciar Compose, defina esas dos variables en el entorno o en un archivo `.env` local (no lo suba al repositorio). Por ejemplo, en PowerShell:

```powershell
$env:CONFIG_ADMIN_USERNAME = "admin"
$env:CONFIG_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new("", (Read-Host "Contraseña del administrador" -AsSecureString)).Password
docker compose up --build
```

Comandos útiles:

```bash
docker compose ps
docker compose logs -f api-gateway
docker compose down
```

`docker compose down` conserva los datos. Para borrar explícitamente los datos locales de la base junto con los contenedores, use `docker compose down -v`.

## Ejecución local con Maven

Puede iniciar cada aplicación desde su carpeta con Maven:

```bash
mvn spring-boot:run
```

Antes, inicie PostgreSQL y cree una base separada para cada servicio (`cooperativa_socios`, `cooperativa_cuentas`, `cooperativa_prestamos` y `cooperativa_usuarios`). Configure las propiedades de conexión, los esquemas y Eureka según el `application.yml` de cada servicio.

## Migraciones de base de datos

Las migraciones Flyway están en:

- `microservices/socio-service/src/main/resources/db/migration`
- `microservices/cuentas-service/src/main/resources/db/migration`
- `microservices/prestamo-service/src/main/resources/db/migration`

Cada servicio aplica sus scripts al esquema correspondiente al iniciar.

## Respuestas de error

Todos los servicios Spring del proyecto cargan el manejador global compartido desde el módulo raíz `common-web`. Los errores se devuelven como JSON con `timestamp`, `status`, `error`, `message`, `path` (cuando está disponible) y `validationErrors`. Se manejan errores de validación y solicitudes mal formadas (400), recursos inexistentes (404), métodos no permitidos (405), conflictos de estado o integridad (409) y errores inesperados (500). Los detalles internos de errores 500 se registran en los logs, no se exponen en la respuesta.

El módulo se registra automáticamente mediante Spring Boot y contiene una única implementación del manejador y del DTO. El gateway usa el mismo componente como `WebExceptionHandler` reactivo; los servicios MVC lo usan como `@RestControllerAdvice`.

## Configuración funcional

La configuración funcional es independiente del `config-server`: contiene datos generales, políticas predeterminadas de préstamos y activación de notificaciones. Se inicializa una sola vez con `POST /api/v1/configuracion`, se consulta con `GET /api/v1/configuracion` y se actualizan secciones mediante `PATCH /api/v1/configuracion/general`, `/api/v1/configuracion/politicas-prestamo` y `/api/v1/configuracion/notificaciones`. El historial paginado está en `GET /api/v1/configuracion/historial` (solo administrador). Las políticas son valores predeterminados y no reemplazan las condiciones de cada producto financiero.

El cuerpo de inicialización requiere los objetos `general`, `loanPolicies` y `notifications`. Los PATCH aceptan solo los campos que se quieran cambiar; se rechaza un parche vacío. La tasa de interés se expresa como porcentaje entre 0 y 100 y la moneda como código ISO de tres letras en mayúsculas.

## Kafka

Kafka todavía no está integrado en los microservicios. Para agregar un broker local y conectar un productor/consumidor Spring Kafka, consulte la [guía de Kafka](GUIA_KAFKA.md).

## GitHub Actions

Para configurar un pipeline de compilación y pruebas con GitHub Actions, consulte la [guía de GitHub Actions](GUIA_GITHUB_ACTIONS.md).
