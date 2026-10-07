# Negocio y alcance

> Sistema de Inventario y Ventas para Papelería. Versión 1.0, octubre de 2026.

## Descripción general

Sistema web para una papelería en Colombia que permite registrar productos leyendo su código de barras (con lector USB/Bluetooth o con la cámara del teléfono), controlar el inventario, registrar ventas en la caja y generar reportes, con especial atención a los productos con poco stock o a punto de agotarse.

Todos los valores monetarios se manejan en **pesos colombianos (COP)**.

### Usuarios del sistema

| Rol | Descripción | Puede |
|---|---|---|
| Administrador | Dueño o encargado de la papelería | Todo: productos, precios, usuarios, reportes, anulaciones, configuración |
| Cajero | Persona que atiende la caja | Vender, consultar precios, abrir y cerrar su caja |
| Bodeguero | Persona que recibe mercancía | Registrar entradas de mercancía, ajustar inventario, imprimir etiquetas |

---

## Alcance

### Dentro del alcance (versión 1)

1. Catálogo de productos con código de barras, categoría, proveedor, precio de venta, costo, tarifa de IVA y stock.
2. Lectura de códigos de barras con lector USB/Bluetooth, cámara del teléfono en vivo y foto.
3. Consulta opcional a un catálogo externo (Open Products Facts) para autocompletar productos nuevos.
4. Generación de códigos internos y etiquetas para productos que no traen código.
5. Registro de entradas de mercancía (compras a proveedores) y ajustes de inventario.
6. Historial de movimientos de inventario (kárdex) por producto.
7. Punto de venta (caja): carrito por escaneo, descuentos, varios medios de pago, cálculo de cambio, tirilla impresa.
8. Anulación de ventas con devolución automática al inventario.
9. Apertura y cierre de caja con cuadre de efectivo.
10. Reportes de stock bajo, productos agotados, productos por agotarse, ventas, productos más vendidos, productos sin movimiento y utilidad.
11. Exportación de reportes a Excel y PDF.
12. Usuarios con roles y contraseñas.

### Fuera del alcance (versión 1)

1. **Facturación electrónica ante la DIAN.** La versión 1 emite tirillas internas (comprobantes de venta no fiscales). Si la papelería está obligada a facturar electrónicamente o a emitir el documento equivalente electrónico POS, se integrará en una versión posterior mediante un proveedor tecnológico autorizado. El modelo de datos ya deja preparados los campos necesarios (cliente, tipo de documento, consecutivo).
2. Ventas en línea o tienda virtual.
3. Manejo de varias sedes o bodegas.
4. Contabilidad completa (solo se exporta información para el contador).
5. Servicios con tarifa por cantidad compleja (fotocopias por volumen, impresión a color por página). En la versión 1 se registran como productos de tipo servicio con precio fijo por unidad.
