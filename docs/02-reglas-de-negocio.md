# Reglas de negocio

Los identificadores RN-xx se usan en el código (comentarios, nombres de pruebas) y en los mensajes de commit. **No renumerar.**

## Moneda y precios

- **RN-01.** Todos los precios, costos, totales y pagos se expresan en pesos colombianos (COP).
- **RN-02.** Los precios se manejan en **pesos enteros, sin centavos**. En la base de datos se guardan como `NUMERIC(12,0)`.
- **RN-03.** En pantalla y en documentos impresos los valores se muestran con el formato colombiano: signo `$`, punto como separador de miles y sin decimales. Ejemplo: `$ 4.500`, `$ 125.000`. En JavaScript: `Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })`. En Java: `NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))` con cero decimales.
- **RN-04.** El precio de venta registrado en el producto es el **precio final al público, con IVA incluido**, porque el Estatuto del Consumidor (Ley 1480 de 2011) exige que el precio informado al consumidor incluya los impuestos.
- **RN-05.** El precio de venta debe ser mayor que cero. El costo puede ser cero (por ejemplo, para un servicio), pero no negativo.
- **RN-06.** El sistema debe advertir, sin bloquear, cuando el precio de venta sea menor o igual al costo.
- **RN-07.** Cada cambio de precio queda registrado en un historial con el precio anterior, el nuevo, la fecha y el usuario que lo hizo.
- **RN-08.** Las ventas guardan el precio unitario vigente en el momento de la venta. Cambiar el precio de un producto después no altera las ventas anteriores.

## IVA

- **RN-09.** Cada producto tiene una tarifa de IVA: 19 % (general), 5 %, 0 % (exento) o excluido.
- **RN-10.** Algunos útiles escolares tienen tratamiento especial de IVA en el Estatuto Tributario (por ejemplo, ciertos cuadernos de tipo escolar). **La tarifa de cada producto la debe validar el contador de la papelería**; el sistema solo la aplica.
- **RN-11.** Como el precio incluye IVA, la base se calcula así: `base = precio / (1 + tarifa)` y `iva = precio − base`. Ambos se redondean al peso más cercano por línea de venta.
- **RN-12.** La tirilla y los reportes muestran el IVA discriminado por tarifa.

## Productos y códigos de barras

- **RN-13.** El código de barras es único: no pueden existir dos productos con el mismo código.
- **RN-14.** Los códigos se normalizan antes de guardarlos o buscarlos: se eliminan espacios, y un UPC-A de 12 dígitos se convierte en EAN-13 agregando un 0 al inicio.
- **RN-15.** Los productos sin código de fábrica reciben un código interno EAN-13 con prefijo `2`, que GS1 reserva para uso interno, con su dígito de control calculado.
- **RN-16.** Los datos que llegan de catálogos externos son solo sugerencias. Una persona debe revisarlos y completar el precio antes de guardar.
- **RN-17.** Los productos no se eliminan si tienen ventas o movimientos; se marcan como inactivos. Un producto inactivo no aparece en la caja ni en las búsquedas normales, pero sí en reportes históricos.
- **RN-18.** Cada producto tiene una unidad de venta: unidad, paquete, resma, caja, pliego, metro o servicio.

## Inventario

- **RN-19.** El stock nunca puede quedar negativo. Si una venta pide más unidades de las disponibles, el sistema la bloquea e informa cuántas hay.
- **RN-20.** Todo cambio de stock genera un movimiento de inventario (kárdex) con tipo, cantidad, stock resultante, fecha, usuario y referencia (venta, compra o ajuste). El stock del producto nunca se modifica directamente sin su movimiento.
- **RN-21.** Tipos de movimiento: `ENTRADA_COMPRA`, `SALIDA_VENTA`, `DEVOLUCION_VENTA` (por anulación), `AJUSTE_POSITIVO`, `AJUSTE_NEGATIVO` y `STOCK_INICIAL`.
- **RN-22.** Los ajustes de inventario exigen un motivo: conteo físico, producto dañado, pérdida, vencimiento u otro (con descripción).
- **RN-23.** El costo del producto se actualiza con **costo promedio ponderado** en cada entrada de mercancía: `nuevo_costo = (stock_actual × costo_actual + cantidad_entrada × costo_entrada) / (stock_actual + cantidad_entrada)`, redondeado al peso.
- **RN-24.** Los servicios (fotocopias, impresiones, plastificados) no manejan stock.
- **RN-46.** El conteo físico se realiza en una sola sesión: el estado vive en el navegador y, al término, las diferencias se aplican al stock en una sola transacción con el motivo "conteo físico" (RN-22).

## Stock bajo y productos por agotarse

- **RN-25.** Cada producto tiene un **stock mínimo** definido por el administrador. Si no se define, se usa el valor por defecto de la configuración (por ejemplo, 5 unidades).
- **RN-26.** Estado de stock de un producto:

| Estado | Condición |
|---|---|
| Agotado | `stock = 0` |
| Stock bajo | `0 < stock ≤ stock_minimo` |
| Por agotarse | `stock > stock_minimo` pero los **días de inventario** son menores al umbral configurado (por defecto 7 días) |
| Normal | Ninguna de las anteriores |

- **RN-27.** Los **días de inventario** se calculan así: `dias = stock / promedio_ventas_diarias`, donde el promedio usa las unidades vendidas en los últimos 30 días (configurable). Si el producto no tuvo ventas en ese periodo, no se calcula y el producto no entra en "por agotarse".
- **RN-28.** La **cantidad sugerida de compra** es: `max(0, promedio_ventas_diarias × dias_cobertura + stock_minimo − stock)`, redondeada hacia arriba, donde `dias_cobertura` es configurable (por defecto 15 días).
- **RN-29.** Al iniciar sesión, el administrador y el bodeguero ven una alerta con el número de productos agotados, con stock bajo y por agotarse.

## Ventas

- **RN-30.** Una venta solo se puede registrar si el cajero tiene una caja abierta.
- **RN-31.** Cada venta recibe un número consecutivo único, sin saltos, generado por el sistema.
- **RN-32.** Al escanear un producto que ya está en el carrito, se suma una unidad a esa línea en lugar de crear otra.
- **RN-33.** Los descuentos pueden ser por línea o sobre el total, en porcentaje o en valor. Un descuento mayor al porcentaje configurado (por ejemplo, 10 %) requiere autorización de un administrador.
- **RN-34.** Medios de pago aceptados: efectivo, tarjeta débito, tarjeta crédito, Nequi, Daviplata y transferencia bancaria. Una venta puede pagarse con varios medios.
- **RN-35.** La suma de los pagos debe ser igual o mayor al total. Solo el efectivo puede exceder el total, y el excedente se devuelve como cambio.
- **RN-36.** El cliente es opcional. Si se registra, se guarda tipo de documento (CC, NIT, CE, pasaporte), número y nombre, para facturación futura.
- **RN-37.** Al confirmar la venta, el sistema descuenta el stock de cada producto y genera los movimientos `SALIDA_VENTA` en la misma transacción. Si algo falla, no se guarda nada.
- **RN-38.** Las ventas no se modifican ni se borran. Solo un administrador puede **anularlas**, indicando el motivo; la anulación devuelve las unidades al inventario con movimientos `DEVOLUCION_VENTA`.
- **RN-39.** Todas las fechas y horas se registran en la zona horaria de Colombia (`America/Bogota`).

## Caja

- **RN-40.** Al abrir la caja se registra la base inicial en efectivo.
- **RN-41.** Al cerrar la caja se registra el efectivo contado. El sistema calcula el efectivo esperado (`base + ventas en efectivo − cambio entregado`) y muestra la diferencia (sobrante o faltante).
- **RN-42.** Un cajero solo puede tener una caja abierta a la vez, y no puede vender después de cerrarla.

## Seguridad

- **RN-43.** Las contraseñas se guardan cifradas con BCrypt.
- **RN-44.** Solo el administrador puede cambiar precios, anular ventas, crear usuarios y ver costos y utilidades.
- **RN-45.** Las acciones sensibles (cambios de precio, ajustes, anulaciones) quedan registradas con usuario, fecha y hora.
