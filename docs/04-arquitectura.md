# Arquitectura

## Decisión

**Monolito modular, paquete por funcionalidad**, con Spring Boot 4 y PostgreSQL. Un solo JAR sirve la API REST y el frontend estático. No hay microservicios ni SPA separada: es un negocio de una sola sede, en red local.

Capas dentro de cada módulo:

```
Controller (REST + DTOs)  →  Service (reglas de negocio y @Transactional)  →  Repository (JPA / JDBC)
```

Las reglas de negocio viven **solo** en los servicios (y en clases puras de cálculo que los servicios usan). Los controladores no calculan nada; los repositorios no deciden nada.

Dos decisiones que se salen de lo habitual:

1. **Reportes sin JPA.** El módulo `reporte` usa `JdbcClient` con SQL directo (ver [consultas-sql.md](consultas-sql.md)) y `record` como resultado.
2. **El stock tiene un solo dueño.** La columna `productos.stock` solo la modifica el módulo `inventario` (RN-20). El módulo `producto` nunca escribe stock.

## Estructura de carpetas

```
papeleria/
├── AGENTS.md                     # instrucciones para OpenCode
├── pom.xml
├── docker-compose.yml            # PostgreSQL 16
├── .env.example
├── docs/                         # esta documentación
├── ops/
│   ├── backup/pg_dump_diario.sh  # RNF-06: copias diarias, 30 días
│   └── caddy/Caddyfile           # HTTPS local para la cámara (RNF-04)
└── src/
    ├── main/
    │   ├── java/com/papeleria/inventario/
    │   │   ├── InventarioApplication.java      # fija TimeZone America/Bogota
    │   │   ├── compartido/                     # sin lógica de negocio
    │   │   │   ├── Pesos.java                  # formato COP y redondeo (RN-02, RN-03)
    │   │   │   └── ZonaHoraria.java
    │   │   ├── error/                          # @RestControllerAdvice y excepciones
    │   │   ├── configuracion/                  # parámetros del negocio (RF-45, RF-46)
    │   │   ├── usuario/                        # usuarios, roles, SecurityConfig, AdminInicialRunner
    │   │   ├── auditoria/                      # RN-45
    │   │   ├── producto/
    │   │   │   ├── api/                        # controladores y DTOs
    │   │   │   ├── servicio/                   # ProductoService, HistorialPreciosService
    │   │   │   ├── dominio/                    # Producto, Categoria, TarifaIva, Unidad
    │   │   │   └── repositorio/
    │   │   ├── proveedor/
    │   │   ├── codigo/                         # ZXing: lectura de foto y etiquetas
    │   │   │   ├── NormalizadorCodigo.java     # UPC-A → EAN-13 (RN-14)
    │   │   │   └── GeneradorCodigoInterno.java # prefijo 2 + dígito de control (RN-15)
    │   │   ├── catalogo/                       # cliente de Open Products Facts (timeout, falla suave)
    │   │   ├── inventario/
    │   │   │   ├── api/
    │   │   │   ├── servicio/
    │   │   │   │   ├── InventarioService.java          # única puerta para cambiar stock
    │   │   │   │   ├── CompraService.java
    │   │   │   │   ├── ConteoFisicoService.java
    │   │   │   │   └── CalculadoraCostoPromedio.java   # RN-23, pura
    │   │   │   ├── dominio/                    # MovimientoInventario, TipoMovimiento, Compra
    │   │   │   └── repositorio/
    │   │   │       └── StockRepository.java    # UPDATE ... WHERE stock >= :cantidad
    │   │   ├── venta/
    │   │   │   ├── api/                        # VentaController, CajaController
    │   │   │   ├── servicio/
    │   │   │   │   ├── VentaService.java       # confirmar() y anular(), @Transactional
    │   │   │   │   ├── CajaService.java        # apertura, cierre, cuadre
    │   │   │   │   ├── ConsecutivoService.java # RN-31
    │   │   │   │   ├── CalculadoraVenta.java   # IVA, descuentos, cambio (pura)
    │   │   │   │   └── TirillaService.java
    │   │   │   ├── dominio/                    # Venta, DetalleVenta, PagoVenta, Caja, MedioPago
    │   │   │   └── repositorio/
    │   │   ├── cliente/
    │   │   └── reporte/
    │   │       ├── api/
    │   │       ├── consulta/                   # una clase por reporte (JdbcClient)
    │   │       └── exportacion/                # ExportadorExcel, ExportadorPdf (genéricos)
    │   └── resources/
    │       ├── application.properties
    │       ├── application-dev.properties
    │       ├── db/migration/                   # V1, V2, ... (Flyway)
    │       └── static/                         # frontend sin paso de compilación
    │           ├── login.html, index.html, caja.html, productos.html, inventario.html, reportes.html
    │           ├── css/                        # app.css, tirilla.css (@page 58/80 mm)
    │           ├── js/                         # api.js, formato.js, escaner.js, caja.js
    │           │   └── vendor/                 # librerías JS copiadas localmente (sin CDN)
    │           └── sonidos/
    └── test/java/com/papeleria/inventario/
        ├── unitarias/                          # calculadoras y generadores (sin Spring)
        ├── integracion/                        # Testcontainers + PostgreSQL real
        └── arquitectura/                       # verifica dependencias entre módulos
```

## Dependencias permitidas entre módulos

| Módulo | Puede usar | No debe usar |
|---|---|---|
| `venta` | `producto`, `inventario`, `cliente`, `configuracion`, `usuario` | modificar `productos.stock` directamente |
| `inventario` | `producto`, `proveedor`, `usuario` | `venta` |
| `producto` | `proveedor`, `configuracion` | `inventario`, `venta` |
| `reporte` | solo lectura por SQL | servicios de otros módulos |
| `compartido`, `error` | nadie los restringe | lógica de negocio |

La dependencia va en un solo sentido: `venta → inventario → producto`. Si `producto` llamara a `inventario` habría un ciclo. Se verifica automáticamente con **ArchUnit** en `test/.../arquitectura/`.

## Dónde vive cada regla crítica

| Regla | Clase | Cómo se cumple |
|---|---|---|
| RN-19 stock no negativo | `StockRepository` | `UPDATE ... WHERE stock >= :cantidad`, más el `CHECK` de la BD |
| RN-20 kárdex siempre | `InventarioService` | Único método que cambia stock y registra el movimiento |
| RN-31 consecutivo sin saltos | `ConsecutivoService` | `UPDATE ... RETURNING` dentro de la transacción de la venta |
| RN-37 venta atómica | `VentaService.confirmar()` | Un solo `@Transactional`: consecutivo, detalles, pagos, descuento de stock |
| RN-11 IVA | `TarifaIva` + `CalculadoraVenta` | Clases puras, probadas con tablas de casos |
| RN-23 costo promedio | `CalculadoraCostoPromedio` | Función pura invocada por `CompraService` |
| RN-44 permisos | `SecurityConfig` + `@PreAuthorize` | Costos y utilidad solo `ADMIN`; los DTOs para otros roles no incluyen costo |
| RN-39 zona horaria | `application.properties` + `InventarioApplication` | `America/Bogota` en JVM, Hibernate y Jackson |

## Frontend

HTML + JavaScript sencillo (Alpine.js opcional) servido desde `static/`, consumiendo la API REST. Motivos: la caja necesita estado en el cliente (carrito, foco del lector, atajos de teclado), no hay paso de compilación y el despliegue es un solo JAR.

- **Lector USB/Bluetooth (RF-09):** el lector actúa como teclado y termina con Enter. `escaner.js` acumula teclas rápidas y dispara la búsqueda al recibir Enter.
- **Cámara (RF-10):** librería local en `static/js/vendor/` (ver [06-dependencias-y-entorno.md](06-dependencias-y-entorno.md)). Requiere HTTPS en producción (RNF-04).
- **Tirilla (RF-26):** en la versión 1, vista HTML con CSS `@page` para 58/80 mm e impresión desde el navegador. PDF con OpenPDF como respaldo.
- **Formato COP (RN-03):** una sola función en `formato.js`; ningún otro archivo formatea dinero.

## Convenciones de código

- Nombres de clases, métodos y tablas en español, como en el esquema (`VentaService`, `confirmar`, `detalle_venta`).
- DTOs como `record`. Las entidades JPA nunca salen por la API.
- Valores fijos (rol, tarifa de IVA, unidad, medio de pago, tipo de movimiento) como `enum` con `@Enumerated(EnumType.STRING)`, coherentes con los `CHECK` del esquema.
- Dinero como `BigDecimal` con escala 0 (o `long`), nunca `double` ni `float`.
- Errores de negocio con excepciones propias, traducidas a respuestas HTTP en `error/`. Mensajes en español de Colombia (RNF-03).
- Cada regla de negocio implementada lleva su código en un comentario o en el nombre de la prueba: `// RN-19`.
