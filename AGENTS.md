# AGENTS.md

API REST para inventario y ventas de papelería en Colombia (COP). Backend monolítico modular: Spring Boot 4.1 + Java 21 + PostgreSQL 16. Frontend aparte (este proyecto no sirve estáticos). **Fuente de verdad: `docs/`** — léela antes de implementar. No inventes reglas de negocio.

## Estado actual (verificado en código)

| Fase | Estado |
|---|---|
| 1. Base | ✅ Completa: `usuario` (login por sesión `JSESSIONID` + BCrypt RN-43, roles `ADMIN`/`CAJERO`/`BODEGUERO`, `SecurityConfig`, `AdminInicialRunner` D-4), `configuracion`, `auditoria`, `compartido` (`Pesos`), `error`, categorías, proveedores. |
| 2. Catálogo | ✅ Backend completo: `producto` (CRUD + activar/inactivar, historial de precios, búsqueda `unaccent`+`pg_trgm` RF-06), `codigo` (normalización RN-14, EAN-13 RN-15, lector ZXing RF-11/12, etiquetas RF-04), `catalogo` (Open Products Facts, falla suave RN-16). Endpoints `POST /api/codigos/decodificar`, `GET /api/codigos/{codigo}/etiqueta`. |
| 3. Inventario | ✅ Completa: `InventarioService` (`descontar`/`devolver`/`ajustar*`, kárdex, stock no negativo vía `UPDATE ... WHERE stock >= :cantidad`), `CalculadoraCostoPromedio`, `CompraService` (RF-14/15, costo promedio ponderado RN-23, rechaza factura repetida), `ConteoFisicoService` (RF-18, RN-46), `CompraController`, kárdex, conteo físico. |
| 4. Caja | ✅ Backend completo: dominio (`Venta`, `DetalleVenta`, `PagoVenta`, `Caja`, `Consecutivo`), `ConsecutivoService` (RN-31), `CalculadoraVenta`, `VentaService` (confirmar/anular, IVA RN-11, descuentos RN-33/D-1, pagos RN-34/35, stock RN-37), `CajaService` (apertura/cierre/cuadre RN-40/41/42), `TirillaService` (HTML @page D-5), `VentaController`, `CajaController`, repos de venta/caja/pagos, `ClienteService`/controller. |
| 5. Reportes | ✅ Backend completo: módulo `reporte` con `JdbcClient` (stock bajo/agotados/por agotarse/pedido por proveedor, ventas por periodo, productos más vendidos/sin movimiento, utilidad por periodo/categoría/producto, valor inventario, cierres de caja, panel) + exportación Excel/PDF (OpenPDF `org.openpdf`). |

- 92 clases en `src/main/java`; 12 archivos de test (38 `@Test` + 1 `@ParameterizedTest`). 7 commits; el último añade gestión de producto (actualizar/activar/inactivar) y roles de seguridad.
- `V1__esquema_inicial.sql` crea **todo** el esquema (todas las fases) + config inicial + `f_unaccent`. `V2__vista_estado_stock.sql`. **No hay V3+**.
- Paquete base: `com.soledad.inventariopapeleria` (no `com.papeleria.inventario`; `docs/04` aún muestra la ruta vieja).

## Qué falta por desarrollar

1. **Transversal:** paquete `integracion` con Testcontainers aún no existe pese a estar en el pom; pruebas con tablas de casos para IVA (RN-11), costo promedio (RN-23), consecutivo (RN-31) y descuentos (D-1).
2. **Decisiones pendientes:** D-1, D-2, D-4 (implementación provisional en `AdminInicialRunner`), D-5, D-6. D-3 resuelta → RN-46. (D-1, D-2, D-5 se implementaron siguiendo la propuesta; marcar `// PENDIENTE-DECISION-n` hasta confirmación.)
3. **Desviación a resolver:** `ProductoService.crear` asigna `stockInicial` directamente (escribe stock desde `producto`); decidir si debe pasar por `InventarioService` (RN-20).
4. **Fase 6 (futura):** facturación electrónica DIAN (fuera de v1).

## Comandos (Windows, usa el wrapper)

- `./mvnw spring-boot:run` — arrancar (requiere PostgreSQL arriba).
- `./mvnw test` — no requiere Docker: el `@SpringBootTest` de contexto conecta a la BD configurada (por defecto `localhost:5435`).
- `./mvnw -DskipTests package` — generar el JAR.
- No hay linter (ni checkstyle/spotless); verificación = compilación + ArchUnit + tests.
- BD: `docker-compose.yml` mapea `5435:5432` (`papeleria_db`/`app_papel`). Copia `.env.example` → `.env` (o define `DB_CLAVE`): el password del compose no coincide con el default de `application.properties`.

## Gotchas del entorno

- `application.properties` y `.env.example` traen credenciales por defecto en claro (`DB_*`); nunca pongas credenciales reales en código. `.env.example` está sin trackear.
- Springdoc expone Swagger UI público en `/swagger-ui.html` y spec en `/v3/api-docs` (permitidos en `SecurityConfig`).
- Actuator en el pom pero sin endpoints configurados ni en `SecurityConfig`: solo `/actuator/health`, cae en `anyRequest().authenticated()` (no público).
- Auth por sesión (no JWT), CSRF desactivado, errores de auth en JSON. `/api/auth/login|logout` público. Matriz: `ADMIN` todo; `BODEGUERO` escribe productos/proveedores (POST/PUT/DELETE) y todo `/api/inventario/**`; `ADMIN` único que administra categorías (`/api/categoria/**` POST/PUT/DELETE) y `/api/usuario|configuracion|auditoria`. Lecturas (GET) de producto/proveedor/categoría para cualquier autenticado.

## Gotchas de Spring Boot 4

- Starter web: `spring-boot-starter-webmvc` (no `starter-web`).
- Flyway: `spring-boot-starter-flyway` + `flyway-database-postgresql` (runtime).
- Jackson 3: clases en `tools.jackson.*`; anotaciones (`@JsonProperty`) siguen en `com.fasterxml.jackson.annotation`.
- Testcontainers 2.x: artefactos con prefijo `testcontainers-`, clase `org.testcontainers.postgresql.PostgreSQLContainer`.
- `pom.xml` tuvo dependencias duplicadas corregidas; evita añadir más.

## Arquitectura (docs/04) — obligatorio

Modular por funcionalidad bajo el paquete base; capas `api (Controller+DTO) → servicio (@Transactional) → dominio → repositorio`.

- Sin ciclos: `venta → inventario → producto`. `producto` NUNCA usa `inventario`/`venta`; `reporte` solo lee por SQL (`JdbcClient`, sin JPA).
- `productos.stock` solo lo modifica `inventario` (RN-20): `InventarioService` + `StockRepository` con `UPDATE ... WHERE stock >= :cantidad` (RN-19).
- Reglas de negocio SOLO en servicios y clases puras; controladores no calculan, repositorios no deciden.
- Dependencias verificadas con ArchUnit en `test/.../arquitectura/` (3 reglas). Saltársela falla el build.

## Código (docs/04 + skills en `.agents/skills/`)

- Nombres y mensajes en español de Colombia (RNF-03). Los RN-xx se usan en comentarios, pruebas y commits. No renumerar.
- Seguir `.agents/skills/java-coding-standards/SKILL.md`, `java-springboot/SKILL.md`, `java-docs/SKILL.md`.
- DTOs como `record`; entidades JPA nunca salen por la API.
- Dinero: `BigDecimal` escala 0 o `long`, nunca `double`/`float` (RN-02). Formato es-CO `$ 12.500` en `compartido.Pesos`, una sola función (RN-03).
- Valores fijos como `enum` con `@Enumerated(EnumType.STRING)`, coherentes con los `CHECK` del esquema.
- Errores de negocio: `ErrorNegocio`/`RecursoNoEncontrado` traducidos a HTTP en `error/` (`GlobalHandler`).
- Fechas solo en `America/Bogota` (RN-39) en JVM, Hibernate (`hibernate.jdbc.time_zone`) y Jackson (`spring.jackson.time-zone`); entidades usan `Instant`.

## Decisiones pendientes (docs/08)

D-1, D-2, D-4, D-5, D-6 NO resueltas. Si tu código toca una, marca `// PENDIENTE-DECISION-n` o pregunta. Al confirmarse pasan a `docs/02` como RN-xx y se borran de `docs/08`.

## Plan (docs/07 + docs/03)

Orden obligatorio. Al terminar cada fase: criterios de aceptación de `docs/07` + pruebas.

1. Fase 1 – Base: ✅ hecha.
2. Fase 2 – Catálogo: ✅ backend completo (frontend por separado RF-09/10/13).
3. Fase 3 – Inventario: ✅ completo.
4. Fase 4 – Caja: ✅ backend completo (ver tabla de estado).
5. Fase 5 – Reportes: ✅ backend completo (módulo `reporte` + exportación).
6. Fase 6 (futura): facturación electrónica DIAN (fuera de v1).

## Pruebas

- Paquetes actuales: `unitarias` (clases puras, sin Spring), `arquitectura` (ArchUnit) y el `@SpringBootTest` de contexto (requiere BD real arriba). Aún no existe `integracion` con Testcontainers.
- Registros críticos (IVA RN-11, costo promedio RN-23, consecutivo RN-31, descuentos D-1) requieren pruebas con tablas de casos, no solo caso feliz.