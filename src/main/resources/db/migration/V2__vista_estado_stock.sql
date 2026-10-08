-- ============================================================
-- V2: Vista v_estado_stock (RN-25 a RN-28, RF-30 a RF-33).
-- Calcula en un solo lugar el estado de cada producto, los dias
-- de inventario y la cantidad sugerida de compra. Lee sus
-- parametros de la tabla configuracion (docs/05, docs/07).
-- ============================================================

CREATE VIEW v_estado_stock AS
WITH cfg AS (
    SELECT
        COALESCE((SELECT NULLIF(valor, '')::numeric     FROM configuracion WHERE clave = 'stock_minimo_defecto'), 5)  AS stock_minimo_defecto,
        COALESCE((SELECT NULLIF(valor, '')::numeric     FROM configuracion WHERE clave = 'dias_agotarse_umbral'), 7)  AS dias_agotarse_umbral,
        COALESCE((SELECT NULLIF(valor, '')::numeric     FROM configuracion WHERE clave = 'dias_promedio_ventas'), 30) AS dias_promedio_ventas,
        COALESCE((SELECT NULLIF(valor, '')::numeric     FROM configuracion WHERE clave = 'dias_cobertura_pedido'), 15) AS dias_cobertura_pedido
),
ventas_recientes AS (
    SELECT dv.producto_id,
           SUM(dv.cantidad)        AS unidades_vendidas,
           MAX(v.fecha)            AS ultima_venta
    FROM detalle_venta dv
    JOIN ventas v ON v.id = dv.venta_id
    WHERE v.estado = 'CONFIRMADA'
      AND v.fecha >= CURRENT_DATE - (SELECT dias_promedio_ventas::int FROM cfg)
    GROUP BY dv.producto_id
)
SELECT p.id AS producto_id,
       p.codigo_barras,
       p.nombre,
       p.categoria_id,
       p.proveedor_id,
       p.stock,
       COALESCE(p.stock_minimo, cfg.stock_minimo_defecto)::int AS stock_minimo,
       p.costo AS costo_promedio,
       COALESCE(vr.unidades_vendidas, 0)::numeric / cfg.dias_promedio_ventas AS promedio_diario,
       CASE
           WHEN vr.unidades_vendidas IS NOT NULL AND vr.unidades_vendidas > 0
               THEN p.stock / (vr.unidades_vendidas::numeric / cfg.dias_promedio_ventas)
       END AS dias_inventario,
       vr.ultima_venta,
       CASE
           WHEN p.stock = 0 THEN 'AGOTADO'
           WHEN p.stock <= COALESCE(p.stock_minimo, cfg.stock_minimo_defecto) THEN 'STOCK_BAJO'
           WHEN vr.unidades_vendidas IS NOT NULL
                AND p.stock / (vr.unidades_vendidas::numeric / cfg.dias_promedio_ventas) < cfg.dias_agotarse_umbral
               THEN 'POR_AGOTARSE'
           ELSE 'NORMAL'
       END AS estado,
       GREATEST(
           0,
           CEIL(
               COALESCE(vr.unidades_vendidas, 0)::numeric / cfg.dias_promedio_ventas * cfg.dias_cobertura_pedido
               + COALESCE(p.stock_minimo, cfg.stock_minimo_defecto)
               - p.stock
           )
       )::int AS sugerido_comprar
FROM productos p
CROSS JOIN cfg
LEFT JOIN ventas_recientes vr ON vr.producto_id = p.id;