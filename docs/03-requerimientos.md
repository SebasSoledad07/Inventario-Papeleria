# Requerimientos

## Requerimientos funcionales

### Productos

| Código | Requerimiento |
|---|---|
| RF-01 | Registrar un producto escaneando su código, con nombre, marca, categoría, proveedor, unidad, precio de venta, costo, tarifa de IVA, stock inicial y stock mínimo. |
| RF-02 | Al escanear un código no registrado, consultar el catálogo externo y prellenar el formulario con los datos encontrados. |
| RF-03 | Generar un código interno para productos sin código de fábrica. |
| RF-04 | Imprimir etiquetas de código de barras con nombre y precio del producto. |
| RF-05 | Editar los datos de un producto y su precio (con historial de precios). |
| RF-06 | Buscar productos por código, nombre, marca o categoría, ignorando mayúsculas y tildes y tolerando errores de digitación ("lapiz" encuentra "Lápiz"). |
| RF-07 | Activar e inactivar productos. |
| RF-08 | Administrar categorías y proveedores. |

### Lectura de códigos de barras

| Código | Requerimiento |
|---|---|
| RF-09 | Leer códigos con lector USB o Bluetooth en modo teclado (el lector envía el código seguido de Enter). |
| RF-10 | Leer códigos con la cámara del teléfono en tiempo real desde el navegador. |
| RF-11 | Leer códigos desde una foto enviada al servidor (decodificación con ZXing). |
| RF-12 | Soportar los formatos EAN-13, EAN-8, UPC-A, UPC-E, Code 128 y Code 39. |
| RF-13 | Dar retroalimentación inmediata al leer un código (sonido y resaltado del código leído). |

### Inventario

| Código | Requerimiento |
|---|---|
| RF-14 | Registrar entradas de mercancía por proveedor, con número de factura del proveedor, productos, cantidades y costo unitario. |
| RF-15 | Actualizar el costo promedio del producto en cada entrada. |
| RF-16 | Registrar ajustes de inventario positivos o negativos con motivo obligatorio. |
| RF-17 | Consultar el kárdex de un producto: todos sus movimientos con fecha, tipo, cantidad, stock resultante y usuario. |
| RF-18 | Realizar conteo físico: escanear productos, registrar la cantidad contada y generar los ajustes por las diferencias. |

### Ventas (caja)

| Código | Requerimiento |
|---|---|
| RF-19 | Abrir caja con base inicial en efectivo. |
| RF-20 | Agregar productos al carrito escaneando o buscando por nombre. |
| RF-21 | Modificar cantidades y quitar productos del carrito. |
| RF-22 | Aplicar descuentos por línea o al total, con autorización si superan el límite. |
| RF-23 | Registrar uno o varios medios de pago y calcular el cambio. |
| RF-24 | Asociar opcionalmente un cliente a la venta. |
| RF-25 | Confirmar la venta descontando el inventario en una sola transacción. |
| RF-26 | Imprimir la tirilla de venta en impresora térmica de 58 u 80 mm, con datos del negocio, consecutivo, productos, IVA discriminado, total, medios de pago y cambio. |
| RF-27 | Consultar ventas por fecha, consecutivo, cajero o cliente. |
| RF-28 | Anular una venta con motivo, devolviendo las unidades al inventario. |
| RF-29 | Cerrar caja con cuadre de efectivo y resumen por medio de pago. |

### Reportes

| Código | Requerimiento |
|---|---|
| RF-30 | **Productos con stock bajo:** lista de productos con `stock ≤ stock_minimo`, con código, nombre, categoría, proveedor, stock actual, stock mínimo y cantidad sugerida de compra. |
| RF-31 | **Productos agotados:** lista de productos con stock cero y la fecha de su última venta. |
| RF-32 | **Productos por agotarse:** productos cuyos días de inventario son menores al umbral, ordenados de menor a mayor número de días, con su promedio de ventas diarias. |
| RF-33 | **Lista de pedido por proveedor:** los productos de RF-30 a RF-32 agrupados por proveedor, con cantidad sugerida y costo estimado en COP, para enviar el pedido. |
| RF-34 | **Ventas por periodo:** totales por día, semana o mes, con número de ventas, ticket promedio, IVA y desglose por medio de pago. |
| RF-35 | **Productos más vendidos:** ranking por unidades y por valor en un periodo. |
| RF-36 | **Productos sin movimiento:** productos sin ventas en los últimos N días (por defecto 60), con el valor de inventario inmovilizado. |
| RF-37 | **Utilidad:** ventas, costo de lo vendido y utilidad bruta por periodo, por categoría y por producto (solo administrador). |
| RF-38 | **Valor del inventario:** stock × costo promedio por producto y total, por categoría. |
| RF-39 | **Cierres de caja:** historial de cierres con diferencias de efectivo. |
| RF-40 | Filtrar todos los reportes por rango de fechas, categoría y proveedor. |
| RF-41 | Exportar todos los reportes a Excel (.xlsx) y PDF, con valores en formato COP. |
| RF-42 | Mostrar en el panel principal los indicadores del día: ventas, número de ventas, productos agotados, con stock bajo y por agotarse. |

### Usuarios y configuración

| Código | Requerimiento |
|---|---|
| RF-43 | Iniciar y cerrar sesión con usuario y contraseña. |
| RF-44 | Crear, editar e inactivar usuarios con rol. |
| RF-45 | Configurar datos del negocio (razón social, NIT, dirección, teléfono, mensaje de la tirilla). |
| RF-46 | Configurar parámetros: stock mínimo por defecto, umbral de días para "por agotarse", días de cálculo del promedio, días de cobertura para pedidos y límite de descuento sin autorización. |

---

## Requerimientos no funcionales

| Código | Requerimiento |
|---|---|
| RNF-01 | La búsqueda de un producto escaneado debe responder en menos de 300 ms en la red local. |
| RNF-02 | La interfaz debe funcionar en navegadores actuales de computador y celular (Chrome, Edge, Safari, Firefox). |
| RNF-03 | La interfaz y los mensajes deben estar en español de Colombia. |
| RNF-04 | La cámara en vivo requiere HTTPS en producción. |
| RNF-05 | La caja debe poder operarse solo con teclado y lector. |
| RNF-06 | Copia de seguridad automática diaria de la base de datos con `pg_dump`, guardando al menos los últimos 30 días. |
| RNF-07 | El esquema de la base de datos se versiona con migraciones (Flyway), no con `ddl-auto=update`. |
| RNF-08 | Las operaciones que tocan inventario y ventas son transaccionales. |
