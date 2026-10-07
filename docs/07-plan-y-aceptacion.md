# Plan de desarrollo y criterios de aceptación

## Fases

| Fase | Contenido | Requerimientos |
|---|---|---|
| 1. Base | Seguridad, usuarios, configuración, Flyway, categorías y proveedores | RF-08, RF-43 a RF-46 |
| 2. Catálogo | Productos, escaneo, catálogo externo, etiquetas, historial de precios | RF-01 a RF-13 |
| 3. Inventario | Compras, ajustes, kárdex, conteo físico | RF-14 a RF-18 |
| 4. Caja | Apertura y cierre, ventas, pagos, tirilla, anulaciones | RF-19 a RF-29 |
| 5. Reportes | Stock bajo, agotados, por agotarse, pedidos, ventas, utilidad, exportación, panel | RF-30 a RF-42 |
| 6. Facturación (futura) | Integración con proveedor tecnológico para facturación electrónica o documento equivalente POS de la DIAN | Fuera de la versión 1 |

---

## Criterios de aceptación clave

- Al escanear un producto registrado, su nombre y precio en COP aparecen en menos de un segundo.
- Una venta confirmada descuenta exactamente las unidades vendidas y deja un movimiento por producto en el kárdex.
- Ninguna operación permite que el stock quede negativo.
- Una venta anulada devuelve las unidades al inventario y no suma en los reportes de ventas.
- El reporte de stock bajo muestra todos y solo los productos activos con `stock ≤ stock_minimo`.
- El reporte de productos por agotarse coincide con el cálculo manual de días de inventario para una muestra de productos.
- Todos los valores monetarios en pantalla, tirillas y exportaciones aparecen con el formato `$ 12.500`.
- El cuadre de caja calcula correctamente la diferencia entre el efectivo esperado y el contado.