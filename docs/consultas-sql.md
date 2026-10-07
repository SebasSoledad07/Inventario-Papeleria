# Consultas de referencia (reportes y operación)

Estas consultas son la referencia para el módulo `reporte` y para los puntos críticos de `inventario` y `venta`. Se ejecutan con `JdbcClient` o consultas nativas, no con JPQL.

**Stock bajo y agotados (RF-30, RF-31):**

```sql
SELECT e.codigo_barras, e.nombre, c.nombre AS categoria, pr.razon_social AS proveedor,
       e.stock, e.stock_minimo, e.estado, e.sugerido_comprar
FROM v_estado_stock e
LEFT JOIN categorias  c  ON c.id  = e.categoria_id
LEFT JOIN proveedores pr ON pr.id = e.proveedor_id
WHERE e.estado IN ('AGOTADO', 'STOCK_BAJO')
ORDER BY e.stock, e.nombre;
```

**Por agotarse (RF-32):**

```sql
SELECT codigo_barras, nombre, stock, stock_minimo, promedio_diario, dias_inventario, sugerido_comprar
FROM v_estado_stock
WHERE estado = 'POR_AGOTARSE'
ORDER BY dias_inventario;
```

**Lista de pedido por proveedor (RF-33):**

```sql
SELECT COALESCE(pr.razon_social, 'Sin proveedor') AS proveedor,
       e.codigo_barras, e.nombre, e.estado, e.sugerido_comprar,
       e.sugerido_comprar * e.costo_promedio AS costo_estimado_cop,
       SUM(e.sugerido_comprar * e.costo_promedio) OVER (PARTITION BY pr.id) AS total_proveedor_cop
FROM v_estado_stock e
LEFT JOIN proveedores pr ON pr.id = e.proveedor_id
WHERE e.estado <> 'NORMAL' AND e.sugerido_comprar > 0
ORDER BY proveedor, e.nombre;
```

**Búsqueda de productos sin tildes y con tolerancia a errores (RF-06):**

```sql
SELECT id, codigo_barras, nombre, precio_venta
FROM productos
WHERE activo
  AND (f_unaccent(lower(nombre)) LIKE '%' || f_unaccent(lower(:texto)) || '%'
       OR f_unaccent(lower(nombre)) % f_unaccent(lower(:texto)))
ORDER BY similarity(f_unaccent(lower(nombre)), f_unaccent(lower(:texto))) DESC
LIMIT 20;
```

**Siguiente consecutivo de venta, sin saltos (RN-31).** Se ejecuta dentro de la misma transacción que guarda la venta; la fila queda bloqueada hasta el commit y, si la venta falla, el número no se consume:

```sql
UPDATE consecutivos SET ultimo = ultimo + 1 WHERE nombre = 'VENTA' RETURNING ultimo;
```

**Descontar stock de forma segura (RN-19).** La condición evita el stock negativo aunque dos cajas vendan el mismo producto al mismo tiempo; si la sentencia no actualiza ninguna fila, no había unidades suficientes y la venta se revierte:

```sql
UPDATE productos SET stock = stock - :cantidad
WHERE id = :productoId AND stock >= :cantidad
RETURNING stock;
```
