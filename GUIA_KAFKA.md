# Guía para usar Apache Kafka en Cooperativa-MS

Esta guía explica cómo agregar mensajería Kafka al proyecto. Actualmente los microservicios del repositorio no tienen Kafka configurado; estos pasos son una propuesta de integración local, no una funcionalidad ya instalada. Los ejemplos usan `prestamo-service` como productor y otro servicio (por ejemplo, `cuentas-service`) como consumidor.

## ¿Para qué usarlo?

Kafka permite publicar eventos sin que el servicio emisor tenga que llamar directamente al servicio receptor. Por ejemplo, cuando se registra un préstamo, `prestamo-service` puede publicar `cooperativa.prestamo.creado.v1` y otros servicios pueden reaccionar al evento. Kafka complementa las llamadas HTTP existentes; no las reemplaza automáticamente.

## 1. Agregar Kafka al entorno local

Agregue este servicio bajo `services:` en el `docker-compose.yml` de la raíz. Es un broker único en modo KRaft, apropiado para desarrollo local, no para producción:

```yaml
  kafka:
    image: apache/kafka:4.1.1
    ports:
      - "29092:29092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093,PLAINTEXT_HOST://:29092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
```

Desde contenedores de Compose, los servicios se conectan a `kafka:9092`. Si ejecuta una aplicación directamente en su máquina, se conecta a `localhost:29092`. No use `localhost` como dirección del broker desde otro contenedor.

Inicie Kafka y cree el tópico de ejemplo:

```powershell
docker compose up -d kafka
docker compose exec kafka kafka-topics.sh --create --topic cooperativa.prestamo.creado.v1 --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1
docker compose exec kafka kafka-topics.sh --list --bootstrap-server kafka:9092
```

Si quiere iniciar un servicio Spring desde Docker Compose, añada `kafka` a su `depends_on` y configure `KAFKA_BOOTSTRAP_SERVERS: kafka:9092` en su entorno. Para desarrollo local, use `localhost:29092`.

## 2. Agregar la dependencia al servicio

En el `pom.xml` del microservicio que enviará o recibirá eventos, agregue dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

Los POM de los microservicios heredan la administración de dependencias de Spring Boot; no es necesario fijar una versión manualmente. Agregue la dependencia solamente a los servicios que realmente usarán Kafka.

## 3. Configurar conexión y serialización

En `microservices/prestamo-service/src/main/resources/application.yaml`, configure el productor. El ejemplo transmite JSON como texto, para mantener simple la configuración del formato:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

app:
  kafka:
    topics:
      prestamo-creado: cooperativa.prestamo.creado.v1
```

En el servicio consumidor, use el mismo broker y tópico, pero un `group-id` propio para ese consumidor:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
    consumer:
      group-id: cuentas-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

app:
  kafka:
    topics:
      prestamo-creado: cooperativa.prestamo.creado.v1
```

`earliest` permite leer desde el inicio cuando el grupo consumidor aún no tiene un offset guardado. Luego de que el grupo procese mensajes, Kafka continuará desde su último offset confirmado.

## 4. Definir el contrato del evento

Mantenga el evento pequeño y estable; no envíe entidades JPA ni datos sensibles que el consumidor no necesite. Por ejemplo:

```java
package com.manudev.cooperativa.prestamo.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PrestamoCreadoEvent(
        UUID prestamoId,
        UUID socioId,
        BigDecimal monto,
        Instant ocurridoEn
) {
}
```

Considere añadir una versión al nombre del tópico o al contrato del mensaje cuando cambie de forma incompatible.

## 5. Publicar un evento desde `prestamo-service`

Inyecte `KafkaTemplate<String, String>` y `ObjectMapper`. Publique el evento cuando la operación de negocio haya sido guardada correctamente:

```java
package com.manudev.cooperativa.prestamo.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class PrestamoEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public PrestamoEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.prestamo-creado}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public CompletableFuture<SendResult<String, String>> publicar(PrestamoCreadoEvent event)
            throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(event);
        return kafkaTemplate.send(topic, event.prestamoId().toString(), payload);
    }
}
```

Use el identificador del préstamo como clave para que los mensajes de un mismo préstamo mantengan su orden dentro de una partición. El servicio que llame a `publicar` debe decidir cómo manejar errores de serialización o publicación: no los ignore ni reporte éxito al usuario si son parte necesaria de la operación.

## 6. Consumir el evento

En el microservicio consumidor, agregue `spring-kafka` y cree un listener. El listener recibe el JSON como texto y lo convierte al contrato:

Defina un DTO local en `cuentas-service` con los mismos campos:

```java
package com.manudev.cooperativa.cuentas.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PrestamoCreadoEvent(
        UUID prestamoId,
        UUID socioId,
        BigDecimal monto,
        Instant ocurridoEn
) {
}
```

Luego, consuma el mensaje:

```java
package com.manudev.cooperativa.cuentas.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PrestamoCreadoListener {
    private final ObjectMapper objectMapper;

    public PrestamoCreadoListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.prestamo-creado}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(String payload) throws IOException {
        PrestamoCreadoEvent event = objectMapper.readValue(payload, PrestamoCreadoEvent.class);
        // Ejecutar aquí la acción necesaria en cuentas-service.
    }
}
```

En una implementación real, conviene mover el contrato a una librería de eventos compartida o mantener DTO equivalentes en cada servicio. Evite que un microservicio dependa del código interno de otro.

## 7. Probar y operar

Puede observar mensajes con el consumidor de consola incluido en la imagen:

```powershell
docker compose exec kafka kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic cooperativa.prestamo.creado.v1 --from-beginning
```

Revise los logs del microservicio para confirmar el resultado de publicación y consumo:

```powershell
docker compose logs -f prestamo-service
docker compose logs -f cuentas-service
```

Antes de usar Kafka en operaciones reales:

- Haga los consumidores idempotentes: Kafka puede volver a entregar un mensaje si hay un fallo o reintento.
- Defina una estrategia de reintentos y mensajes fallidos (dead-letter topic); no descarte silenciosamente mensajes inválidos.
- Para mantener PostgreSQL y Kafka consistentes al publicar eventos que dependen de una transacción de base de datos, evalúe el patrón Outbox. Guardar en PostgreSQL y publicar directamente a Kafka son dos operaciones distintas y pueden fallar por separado.
- En producción configure varios brokers, autenticación, cifrado TLS, retención, particiones, monitoreo y copias de seguridad. La configuración local de un solo broker no ofrece alta disponibilidad.
