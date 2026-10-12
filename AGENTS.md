# AGENTS.md

API REST para un sistema de inventario y ventas de papelería en Colombia (COP). Backend monolítico modular con Spring Boot 4.1 + Java 21 + PostgreSQL 16. El frontend se desarrollará por separado (no se sirve desde este proyecto; `src/main/resources/static/` está vacío). La **fuente de verdad es `docs/`**: léela antes de implementar cualquier cosa. **No inventes reglas de negocio.**

## Estado real (verificado en el código)

| Fase | Estado |
|---|---|
| **1. Base** | ✅ Completa: `usuario` (login por sesión HTTP `JSESSIONID` + BCrypt RN-43, roles `ADMIN`/`CAJERO`/`BODEGUERO`, `SecurityConfig`, `AdminInicialRunner` — D-4 lee `ADMIN_INICIAL_USUARIO`/`ADMIN_INICIAL_CLAVE` de entorno, si falta no crea nada), `configuracion`, `auditoria`, `compartido` (solo `Pesos`), `error`, categorías y proveedores. |
| **2. Catálogo** | ✅ Backend completo: `producto` (CRUD, historial de precios, búsqueda `unaccent`+`pg_trgm` RF-06), `codigo` (`NormalizadorCodigo` RN-14 + `GeneradorCodigoInterno` RN-15, `LectorCodigoFoto` ZXing RF-11/12, `GeneradorEtiqueta` RF-04), `catalogo` (cliente Open Products Facts con `RestClient`). Endpoints `POST /api/codigos/decodificar` y `GET /api/codigos/{codigo}/etiqueta`. RF-09/10/13 (lector USB, cámara y feedback) son del frontend, por separado. |
| **3. Inventario** | ✅ Completa: `InventarioService` (`descontar`/`devolver`/`ajustar*`, kárdex, stock no negativo vía `UPDATE ... WHERE stock >= :cantidad`), `CalculadoraCostoPromedio` (pura), `CompraService` (RF-14/15, costo promedio ponderado RN-23, rechaza factura repetida), `ConteoFisicoService` (RF-18, RN-46, ajustes con motivo `"conteo físico"`), `CompraController` (POST/GET `/api/inventario/compras`), GET `/api/inventario/productos/{id}/kardex`, POST `/api/inventario/conteo-fisico`. |
| **4. Caja** | ◐ Parcial: dominio completo (`Venta`, `DetalleVenta`, `PagoVenta`, `Caja`, `Consecutivo`), `ConsecutivoService` (RN-31), `CalculadoraVenta` (pura). **Falta:** `VentaService`, `CajaService`, `TirillaService`, controllers y repos de venta/caja/pagos; `cliente` tiene solo la entidad. |
| **5. Reportes** | ✖ No iniciada: no existe el módulo `reporte`. |

- 92 clases en `src/main/java`, 39 tests verdes. Últimos 4 commits: esquemas de dominio/API para inventario, producto y venta.
- `V1__esquema_inicial.sql` ya crea **todo** el esquema (todas las fases: kárdex, compras, clientes, cajas, ventas, `consecutivos`) + datos iniciales de config + `f_unaccent`. `V2__vista_estado_stock.sql`. **No hay V3+ todavía.**
- Paquete base: **`com.soledad.inventariopapeleria`** (no `com.papeleria.inventario`; `docs/04` aún muestra la ruta vieja).

## Comandos (Windows, usa el wrapper)

- `./mvnw spring-boot:run` — arrancar (requiere PostgreSQL arriba).
- `./mvnw test` — **no requiere Docker**: el único test de contexto (`@SpringBootTest`) conecta a la BD configurada (por defecto `localhost:5435`). Las dependencias Testcontainers están en el pom pero **no hay aún tests de `integracion` que las usen**.
- `./mvnw -DskipTests package` — generar el JAR.
- No hay linter configurado (ni checkstyle ni spotless); la verificación es compilación + ArchUnit + tests.

## Gotchas del entorno

- `docker-compose.yml` mapea `5435:5432` y crea `papeleria_db`/`app_papel`, igual que el default de la app (`jdbc:postgresql://localhost:5435/papeleria_db`). El **password** default del compose es un placeholder: para conectar copia `.env.example` → `.env` (o define `DB_CLAVE`), porque NO coincide con el default de `application.properties`.
- `application.properties` y `.env.example` traen credenciales por defecto en claro (variables `DB_*`); nunca pongas credenciales reales en código. `.env.example` está sin trackear en git.
- **Springdoc** (dependencia `springdoc-openapi-starter-webmvc-ui` 3.1.1) expone Swagger UI público en `/swagger-ui.html` y el spec en `/v3/api-docs` (permitidos en `SecurityConfig`; para pruebas del frontend).
- Auth es **por sesión** (no JWT), CSRF desactivado, errores de autenticación en JSON. `/api/auth/login|logout` público; `/api/usuario`, `/api/configuracion`, `/api/auditoria` solo `ADMIN`.

## Gotchas de Spring Boot 4

- El starter web es `spring-boot-starter-webmvc` (ya no `starter-web`).
- Flyway necesita `spring-boot-starter-flyway` + `flyway-database-postgresql` (runtime) para auto-configurarse.
- Jackson 3: las clases están en `tools.jackson.*`; las anotaciones (`@JsonProperty`) siguen en `com.fasterxml.jackson.annotation`.
- Testcontainers 2.x: artefactos con prefijo `testcontainers-`, clase `org.testcontainers.postgresql.PostgreSQLContainer`.
- `pom.xml` tuvo dependencias duplicadas corregidas; evita añadir más duplicados.

## Arquitectura (docs/04) — obligatorio

API REST modular por funcionalidad bajo el paquete base; en cada módulo las capas `api (Controller+DTO) → servicio (@Transactional) → dominio → repositorio`.

- Sin ciclos de dependencia: `venta → inventario → producto`. `producto` NUNCA usa `inventario`/`venta`; `reporte` solo lee por SQL (`JdbcClient`, sin JPA).
- **`productos.stock` solo lo modifica el módulo `inventario`** (RN-20): `InventarioService` + `StockRepository` con `UPDATE ... WHERE stock >= :cantidad` (RN-19). **Desviación actual:** `ProductoService.crear` asigna `stockInicial` directamente (escribe stock desde `producto`). Decidir si debe pasar por `InventarioService`.
- Reglas de negocio SOLO en servicios y clases puras; los controladores no calculan, los repositorios no deciden.
- Las dependencias se verifican con **ArchUnit** en `test/.../arquitectura/` (3 reglas: `producto` no usa `inventario`/`venta`; `venta` no toca `StockRepository`; `reporte` no usa servicios ajenos). Saltársela falla el build.

## Código (docs/04 + skills en `.agents/skills/`)

- Nombres y mensajes en español de Colombia (RNF-03): `VentaService`, `confirmar`, `detalle_venta`. Los RN-xx se usan en comentarios, nombres de pruebas y mensajes de commit. **No renumerar.**
- Seguir `.agents/skills/java-coding-standards/SKILL.md` (inmutabilidad, `record`, streams cortos, `Optional` en `find*`, excepciones propias, log SLF4J) y `.agents/skills/java-springboot/SKILL.md` (inyección por constructor con campos `final`, Javadoc según `.agents/skills/java-docs/SKILL.md`).
- DTOs como `record`; las entidades JPA nunca salen por la API.
- Dinero: `BigDecimal` escala 0 o `long`, nunca `double`/`float` (RN-02). Formato es-CO `$ 12.500` en `compartido.Pesos`, una sola función (RN-03).
- Valores fijos como `enum` con `@Enumerated(EnumType.STRING)`, coherentes con los `CHECK` del esquema (roles, unidad, tarifa_iva, tipo movimiento, medio pago, estado).
- Errores de negocio: excepciones propias (`ErrorNegocio`, `RecursoNoEncontrado`) traducidas a HTTP en el módulo `error/` (`GlobalHandler`).
- Fechas solo en `America/Bogota` (RN-39), en JVM, Hibernate (`hibernate.jdbc.time_zone`) y Jackson (`spring.jackson.time-zone`); entidades usan `Instant`.

## Decisiones pendientes (docs/08)

D-1, D-2, D-4, D-5 y D-6 NO están resueltas (D-4 tiene implementación provisional en `AdminInicialRunner`, marcada `// PENDIENTE-DECISION-4`). **D-3 ya está resuelta** → RN-46 (conteo físico en una sola sesión). Si tu código toca una decisión pendiente, marca la elección con `// PENDIENTE-DECISION-n` o pregunta. Al confirmarse pasan a `docs/02` como RN-xx y se borran de `docs/08`.

## Plan paso a paso (docs/07 + docs/03)

Orden obligatorio. Al terminar cada fase: criterios de aceptación de `docs/07` + pruebas unitarias/integración/arquitectura.

1. **Fase 1 – Base (RF-08, RF-43 a RF-46):** ✅ hecha. **Una migración aplicada nunca se edita:** los cambios son `V3__...`, `V4__...`.
2. **Fase 2 – Catálogo (RF-01 a RF-13):** módulos `producto`, `proveedor`, `codigo` (normalización RN-14, código interno EAN-13 RN-15, **lectura de foto ZXing RF-11/12, etiquetas RF-04**), `catalogo` (Open Products Facts con timeout y falla suave RN-16), historial de precios (RN-07/08), búsqueda `unaccent`+`pg_trgm` (RF-06). Frontend por separado (RF-09/10/13).
3. **Fase 3 – Inventario (RF-14 a RF-18):** ✅ compras con costo promedio ponderado (RN-23), ajustes con motivo obligatorio (RN-22), kárdex (RN-20), conteo físico (RN-46). Stock nunca negativo (RN-19). Servicios sin stock (RN-24).
4. **Fase 4 – Caja (RF-19 a RF-29):** caja abierta obligatoria (RN-30/42), consecutivo sin saltos `ConsecutivoService` (RN-31, ✅), venta atómica en un solo `@Transactional` (RN-37), medios de pago y cambio (RN-34/35), tirilla, anulación con devolución (RN-38, restringida por D-2), descuentos con autorización ADMIN (RN-33, D-6).
5. **Fase 5 – Reportes (RF-30 a RF-42):** módulo `reporte` con `JdbcClient` y las consultas de `docs/consultas-sql.md`, exportación Excel/PDF (OpenPDF: paquete `org.openpdf`, no `com.lowagie`), panel. Costos y utilidad solo ADMIN (RN-44).
6. **Fase 6 (futura):** facturación electrónica DIAN. Fuera de la versión 1.

## Pruebas

- Paquetes actuales: `unitarias` (clases puras, sin Spring), `arquitectura` (ArchUnit), y el `@SpringBootTest` de contexto (requiere la BD real arriba). **Aún no existe el paquete `integracion` con Testcontainers** pese a lo que dicen la doc y el pom.
- Registros críticos (IVA RN-11, costo promedio RN-23, consecutivo RN-31, descuentos D-1) requieren pruebas con tablas de casos (descuentos que no dividen exacto, etc.), no solo el caso feliz.