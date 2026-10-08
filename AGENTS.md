# AGENTS.md

API REST para un sistema de inventario y ventas de papelería en Colombia (COP). Backend monolítico modular con Spring Boot 4.1 + Java 21 + PostgreSQL 16. El frontend se desarrollará por separado (no se sirve desde este proyecto). La **fuente de verdad es `docs/`**: léela antes de implementar cualquier cosa. **No inventes reglas de negocio.**

## Estado real (tras la Fase 1)

- `src/` contiene la API REST: módulos `compartido`, `error`, `auditoria`, `configuracion`, `usuario`, `producto`, `proveedor` ya implementados en base (RF-08, RF-43 a RF-46). Aún faltan los demás módulos.
- `application.properties` ya configurado (zona horaria America/Bogota, `ddl-auto=validate`, `open-in-view=false`, Flyway).
- Existen `docker-compose.yml`, `.env.example`, migraciones `V1__esquema_inicial.sql` y `V2__vista_estado_stock.sql` en `db/migration`.
- Paquete base: **`com.soledad.inventariopapeleria`** (no `com.papeleria.inventario`).

## Comandos (Windows, usa el wrapper)

- `./mvnw spring-boot:run` — arrancar (requiere PostgreSQL arriba)
- `./mvnw test` — pruebas (requiere Docker/Testcontainers; **no hay H2**, las pruebas usan PostgreSQL real)
- `./mvnw -DskipTests package` — generar el JAR
- Preferir `mvnw` sobre un `mvn` instalado (reproducibilidad).

## Gotchas de Spring Boot 4

- El starter web es `spring-boot-starter-webmvc` (ya no `starter-web`).
- Flyway necesita `spring-boot-starter-flyway` + `flyway-database-postgresql` (runtime) para auto-configurarse.
- Jackson 3: las clases están en `tools.jackson.*`; las anotaciones (`@JsonProperty`) siguen en `com.fasterxml.jackson.annotation`.
- Testcontainers 2.x: artefactos con prefijo `testcontainers-`, clase `org.testcontainers.postgresql.PostgreSQLContainer`.
- `pom.xml` tiene dependencias duplicadas corregidas parcialmente; evita añadir más duplicados.

## Arquitectura (docs/04) — obligatorio

API REST modular por funcionalidad bajo el paquete base; en cada módulo las capas `api (Controller+DTO) → servicio (@Transactional) → dominio → repositorio`.

- Sin ciclos de dependencia: `venta → inventario → producto`. `producto` NUNCA usa `inventario`/`venta`; `reporte` solo lee por SQL (`JdbcClient`, sin JPA) con las consultas de `docs/consultas-sql.md`.
- **`productos.stock` solo lo modifica el módulo `inventario`** (RN-20): `InventarioService` + `StockRepository` con `UPDATE ... WHERE stock >= :cantidad` para garantizar stock no negativo (RN-19).
- Reglas de negocio SOLO en servicios y clases puras de cálculo; los controladores no calculan, los repositorios no deciden.
- Las dependencias se verifican con **ArchUnit** en `test/.../arquitectura/`: construir fuera de la matriz falla el build.

## Código (docs/04 + skills en `.agents/skills/`)

- Nombres y mensajes en español de Colombia (RNF-03): `VentaService`, `confirmar`, `detalle_venta`. Los RN-xx se usan en comentarios, nombres de pruebas y mensajes de commit. **No renumerar.**
- Seguir `.agents/skills/java-coding-standards/SKILL.md` (inmutabilidad, `record`, streams cortos, `Optional` en `find*`, excepciones propias, log SLF4J) y `.agents/skills/java-springboot/SKILL.md` (inyección por constructor con campos `final`, Javadoc según `.agents/skills/java-docs/SKILL.md`).
- DTOs como `record`; las entidades JPA nunca salen por la API.
- Dinero: `BigDecimal` escala 0 o `long`, nunca `double`/`float` (RN-02). Formato es-CO `$ 12.500`, una sola función para formatear en el frontend (RN-03).
- Valores fijos como `enum` con `@Enumerated(EnumType.STRING)`, coherentes con los `CHECK` del esquema.
- Errores de negocio: excepciones propias traducidas a HTTP en el módulo `error/` (`@RestControllerAdvice`).
- Fechas solo en `America/Bogota` (RN-39), en JVM, Hibernate y Jackson.

## Decisiones pendientes (docs/08)

D-1 a D-6 NO están resueltas. Si tu código las toca, marca la elección con `// PENDIENTE-DECISION-n` o pregunta. Al confirmarse pasan a `docs/02` como RN-xx y se borran de `docs/08`.

## Plan paso a paso (docs/07 + docs/03)

Orden obligatorio. Al terminar cada fase: criterios de aceptación de `docs/07` + pruebas unitarias/integración/arquitectura.

1. **Fase 1 – Base (RF-08, RF-43 a RF-46):** seguridad y usuarios (BCrypt RN-43, roles, `SecurityConfig`, `AdminInicialRunner` D-4), `configuracion` (RF-45/46), `application.properties` (timezone, `ddl-auto=validate`, `open-in-view=false`, Flyway), `docker-compose.yml` + `.env` (claves solo por variables de entorno), migraciones `V1__esquema_inicial.sql` y `V2__vista_estado_stock.sql` según `docs/05`, módulos `usuario`, `auditoria` (RN-45), `compartido`, `error`, categorías y proveedores. **Una migración aplicada nunca se edita:** los cambios son `V3__...`, `V4__...`.
2. **Fase 2 – Catálogo (RF-01 a RF-13):** módulos `producto`, `proveedor`, `codigo` (normalización RN-14, código interno EAN-13 RN-15), `catalogo` (Open Products Facts con timeout y falla suave RN-16), historial de precios (RN-07/08), búsqueda con `unaccent`+`pg_trgm` (RF-06), etiquetas y lectura de códigos (ZXing). Frontend por separado.
3. **Fase 3 – Inventario (RF-14 a RF-18):** módulo `inventario`: compras con costo promedio ponderado (RN-23), ajustes con motivo obligatorio (RN-22), kárdex (RN-20), conteo físico (D-3). Stock nunca negativo (RN-19). Servicios sin stock (RN-24).
4. **Fase 4 – Caja (RF-19 a RF-29):** módulos `venta` + `cliente`: caja abierta obligatoria (RN-30/42), consecutivo sin saltos `ConsecutivoService` (RN-31), venta atómica en un solo `@Transactional` (RN-37), medios de pago y cambio (RN-34/35), tirilla, anulación con devolución (RN-38, restringida por D-2), descuentos con autorización ADMIN (RN-33, D-6).
5. **Fase 5 – Reportes (RF-30 a RF-42):** módulo `reporte` con `JdbcClient`, exportación Excel/PDF (OpenPDF: paquete `org.openpdf`, no `com.lowagie`), panel. Costos y utilidad solo ADMIN (RN-44).
6. **Fase 6 (futura):** facturación electrónica DIAN. Fuera de la versión 1.

## Pruebas

- Paquetes de test: `unitarias` (clases puras, sin Spring), `integracion` (Testcontainers + PostgreSQL real con `@ServiceConnection`), `arquitectura` (ArchUnit).
- Registros críticos (IVA RN-11, costo promedio RN-23, consecutivo RN-31, descuentos D-1) requieren pruebas con tablas de casos (descuentos que no dividen exacto, etc.), no solo el caso feliz.