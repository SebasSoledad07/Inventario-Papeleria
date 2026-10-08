# Dependencias y entorno

Versiones verificadas en octubre de 2026. El `pom.xml` de la raíz ya las incluye.

## Software para desarrollar

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK (Eclipse Temurin) | 21 (mínimo 17) | Compilar y ejecutar |
| Maven | 3.9+ (o el wrapper `mvnw`) | Dependencias y build |
| PostgreSQL | 16+ | Base de datos (vía Docker o instalación directa) |
| Docker | Reciente | PostgreSQL local y pruebas con Testcontainers (obligatorio para las pruebas) |
| Git | Reciente | Control de versiones |
| DBeaver o pgAdmin 4 | Reciente | Consultar la base de datos |
| OpenCode | Reciente | Asistente de desarrollo (lee `AGENTS.md`) |

## Dependencias Maven

### Spring Boot 4.1 (versiones gestionadas por el parent)

| Dependencia | Uso |
|---|---|
| `spring-boot-starter-webmvc` | API REST y servir `static/`. **Antes se llamaba `starter-web`.** |
| `spring-boot-starter-data-jpa` | Entidades y repositorios |
| `spring-boot-starter-jdbc` | `JdbcClient` para reportes |
| `spring-boot-starter-validation` | Validación de DTOs |
| `spring-boot-starter-security` | Login, roles, BCrypt |
| `spring-boot-starter-restclient` | `RestClient` para el catálogo externo |
| `org.postgresql:postgresql` | Driver (runtime) |
| `spring-boot-starter-flyway` | Migraciones. **En Boot 4 `flyway-core` solo ya no basta.** |
| `org.flywaydb:flyway-database-postgresql` | Soporte de PostgreSQL en Flyway (runtime) |

### Fuera del BOM (versión propia)

| Dependencia | Versión | Uso |
|---|---|---|
| `com.google.zxing:core` y `javase` | 3.5.4 | Leer códigos desde foto y generar etiquetas |
| `org.apache.poi:poi-ooxml` | 5.5.1 | Exportar a Excel |
| `com.github.librepdf:openpdf` | 3.0.3 | Exportar a PDF. **Paquete Java `org.openpdf`, no `com.lowagie`.** |

### Pruebas

| Dependencia | Uso |
|---|---|
| `spring-boot-starter-test` | JUnit 5, AssertJ, Mockito |
| `spring-boot-starter-webmvc-test`, `-data-jpa-test`, `-security-test`, `-flyway-test` | Starters de prueba por tecnología (nuevos en Boot 4) |
| `spring-boot-testcontainers` | Integración con Spring (`@ServiceConnection`) |
| `org.testcontainers:testcontainers-junit-jupiter` y `testcontainers-postgresql` | PostgreSQL real en Docker. **Testcontainers 2.x usa el prefijo `testcontainers-` y la clase `org.testcontainers.postgresql.PostgreSQLContainer`.** |
| `com.tngtech.archunit:archunit-junit5` (1.4.1) | Verifica las dependencias entre módulos |

**No se usa H2.** El esquema usa extensiones, índices parciales, triggers y vistas con `FILTER` propios de PostgreSQL. Las pruebas corren contra PostgreSQL real.

## Cambios de Spring Boot 4 que afectan al código

El borrador original asumía nombres de Spring Boot 3. Con Boot 4 hay que tener en cuenta:

- `spring-boot-starter-web` → `spring-boot-starter-webmvc`.
- Flyway necesita `spring-boot-starter-flyway`; sin él no se auto-configura.
- Jackson 3: las clases (`JsonMapper`, etc.) están en `tools.jackson.*`. Las anotaciones (`@JsonProperty`) siguen en `com.fasterxml.jackson.annotation`.
- Testcontainers 2.x: artefactos y paquetes renombrados (ver arriba).
- Cada starter tiene su starter de pruebas (`-test`).

Si el proyecto existente está en Spring Boot 3.5, ya no tiene soporte de código abierto (terminó el 30-jun-2026). Conviene migrarlo a 4.x antes de seguir.

## Frontend

El frontend será desarrollado en un proyecto separado que consuma esta API REST. No se entregará desde este backend. Para el lector de códigos y la tirilla, en el frontend se usarán las convenciones especificadas en los RFs (lector tipo teclado, `barcode-detector` si se usa cámara, y formato COP con una sola función).

## Instalación de PostgreSQL con Docker

```bash
cp .env.example .env      # y cambiar las claves
docker compose up -d
```

## Configuración

`src/main/resources/application.properties` ya trae la zona horaria `America/Bogota`, `ddl-auto=validate`, `open-in-view=false` y Flyway activado. Las claves salen de variables de entorno (`.env`), nunca del código.

## Hardware recomendado

- Lector de código de barras USB o Bluetooth en modo HID (teclado), con distribución de teclado en español y sufijo Enter.
- Impresora térmica de tirillas de 58 u 80 mm.
- Impresora de etiquetas (opcional) para productos con código interno.
- Cajón monedero (opcional), conectado a la impresora térmica.

## Comandos habituales

```bash
./mvnw spring-boot:run        # arrancar (necesita PostgreSQL arriba)
./mvnw test                   # pruebas (necesita Docker)
./mvnw -DskipTests package    # generar el JAR
```
