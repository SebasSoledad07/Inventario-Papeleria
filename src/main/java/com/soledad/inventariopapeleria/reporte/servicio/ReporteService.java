package com.soledad.inventariopapeleria.reporte.servicio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class ReporteService {

    private final JdbcClient jdbc;

    public ReporteService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> stockBajo() {
        return jdbc.sql("""
                SELECT e.codigo_barras, e.nombre, c.nombre AS categoria, pr.razon_social AS proveedor,
                       e.stock, e.stock_minimo, e.estado, e.sugerido_comprar
                FROM v_estado_stock e
                LEFT JOIN categorias  c  ON c.id  = e.categoria_id
                LEFT JOIN proveedores pr ON pr.id = e.proveedor_id
                WHERE e.estado IN ('AGOTADO', 'STOCK_BAJO')
                ORDER BY e.stock, e.nombre
                """).query(this::mapRow).list();
    }

    public List<Map<String, Object>> agotados() {
        return jdbc.sql("""
                SELECT e.codigo_barras, e.nombre, c.nombre AS categoria, pr.razon_social AS proveedor,
                       e.stock, e.ultima_venta
                FROM v_estado_stock e
                LEFT JOIN categorias  c  ON c.id  = e.categoria_id
                LEFT JOIN proveedores pr ON pr.id = e.proveedor_id
                WHERE e.estado = 'AGOTADO'
                ORDER BY e.ultima_venta
                """).query(this::mapRow).list();
    }

    public List<Map<String, Object>> porAgotarse() {
        return jdbc.sql("""
                SELECT codigo_barras, nombre, stock, stock_minimo, promedio_diario, dias_inventario, sugerido_comprar
                FROM v_estado_stock
                WHERE estado = 'POR_AGOTARSE'
                ORDER BY dias_inventario
                """).query(this::mapRow).list();
    }

    public List<Map<String, Object>> pedidoProveedor() {
        return jdbc.sql("""
                SELECT COALESCE(pr.razon_social, 'Sin proveedor') AS proveedor,
                       e.codigo_barras, e.nombre, e.estado, e.sugerido_comprar,
                       e.sugerido_comprar * e.costo_promedio AS costo_estimado_cop,
                       SUM(e.sugerido_comprar * e.costo_promedio) OVER (PARTITION BY pr.id) AS total_proveedor_cop
                FROM v_estado_stock e
                LEFT JOIN proveedores pr ON pr.id = e.proveedor_id
                WHERE e.estado <> 'NORMAL' AND e.sugerido_comprar > 0
                ORDER BY proveedor, e.nombre
                """).query(this::mapRow).list();
    }

    public Map<String, Object> ventasPeriodo(LocalDate desde, LocalDate hasta, String agrupacion) {
        String trunc = switch (agrupacion == null ? "dia" : agrupacion.toLowerCase()) {
            case "semana" -> "week";
            case "mes" -> "month";
            default -> "day";
        };
        String sqlPeriodos = "SELECT date_trunc('" + trunc + "', fecha) AS periodo, "
                + "COUNT(*) AS numero_ventas, COALESCE(SUM(total), 0) AS total, "
                + "COALESCE(AVG(total), 0) AS ticket_promedio, COALESCE(SUM(iva_total), 0) AS iva_total "
                + "FROM ventas WHERE estado = 'CONFIRMADA' AND fecha::date BETWEEN :desde AND :hasta "
                + "GROUP BY 1 ORDER BY 1";
        List<Map<String, Object>> periodos = jdbc.sql(sqlPeriodos)
                .param("desde", desde)
                .param("hasta", hasta)
                .query(this::mapRow).list();
        List<Map<String, Object>> pagos = jdbc.sql("""
                SELECT p.medio, COALESCE(SUM(p.valor), 0) AS valor
                FROM pagos_venta p
                JOIN ventas v ON v.id = p.venta_id
                WHERE v.estado = 'CONFIRMADA' AND v.fecha::date BETWEEN :desde AND :hasta
                GROUP BY p.medio ORDER BY p.medio
                """)
                .param("desde", desde)
                .param("hasta", hasta)
                .query(this::mapRow).list();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("periodos", periodos);
        res.put("pagos", pagos);
        return res;
    }

    public List<Map<String, Object>> productosMasVendidos(LocalDate desde, LocalDate hasta, int limite) {
        return jdbc.sql("""
                SELECT p.id, p.codigo_barras, p.nombre,
                       COALESCE(SUM(dv.cantidad), 0) AS unidades,
                       COALESCE(SUM(dv.total_linea), 0) AS valor
                FROM detalle_venta dv
                JOIN ventas v ON v.id = dv.venta_id
                JOIN productos p ON p.id = dv.producto_id
                WHERE v.estado = 'CONFIRMADA' AND v.fecha::date BETWEEN :desde AND :hasta
                GROUP BY p.id, p.codigo_barras, p.nombre
                ORDER BY unidades DESC
                LIMIT :limite
                """)
                .param("desde", desde)
                .param("hasta", hasta)
                .param("limite", limite)
                .query(this::mapRow).list();
    }

    public List<Map<String, Object>> productosSinMovimiento(int dias) {
        return jdbc.sql("""
                SELECT p.codigo_barras, p.nombre, p.stock, p.costo,
                       (p.stock * p.costo) AS valor_inmovilizado
                FROM productos p
                WHERE p.activo
                  AND NOT EXISTS (
                        SELECT 1 FROM detalle_venta dv
                        JOIN ventas v ON v.id = dv.venta_id
                        WHERE dv.producto_id = p.id
                          AND v.estado = 'CONFIRMADA'
                          AND v.fecha >= CURRENT_DATE - :dias
                  )
                ORDER BY valor_inmovilizado DESC
                """)
                .param("dias", dias)
                .query(this::mapRow).list();
    }

    public List<Map<String, Object>> utilidadPeriodo(LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                SELECT date_trunc('month', v.fecha) AS periodo,
                       COALESCE(SUM(v.total), 0) AS ventas,
                       COALESCE(SUM(dv.cantidad * p.costo), 0) AS costo_venta,
                       COALESCE(SUM(v.total), 0) - COALESCE(SUM(dv.cantidad * p.costo), 0) AS utilidad_bruta
                FROM ventas v
                JOIN detalle_venta dv ON dv.venta_id = v.id
                JOIN productos p ON p.id = dv.producto_id
                WHERE v.estado = 'CONFIRMADA' AND v.fecha::date BETWEEN :desde AND :hasta
                GROUP BY 1 ORDER BY 1
                """)
                .param("desde", desde)
                .param("hasta", hasta)
                .query(this::mapRow).list();
    }

    public List<Map<String, Object>> utilidadCategoria(LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                SELECT c.nombre AS categoria,
                       COALESCE(SUM(dv.total_linea), 0) AS ventas,
                       COALESCE(SUM(dv.cantidad * p.costo), 0) AS costo_venta,
                       COALESCE(SUM(dv.total_linea), 0) - COALESCE(SUM(dv.cantidad * p.costo), 0) AS utilidad_bruta
                FROM detalle_venta dv
                JOIN ventas v ON v.id = dv.venta_id
                JOIN productos p ON p.id = dv.producto_id
                LEFT JOIN categorias c ON c.id = p.categoria_id
                WHERE v.estado = 'CONFIRMADA' AND v.fecha::date BETWEEN :desde AND :hasta
                GROUP BY c.nombre ORDER BY utilidad_bruta DESC
                """)
                .param("desde", desde)
                .param("hasta", hasta)
                .query(this::mapRow).list();
    }

    public List<Map<String, Object>> utilidadProducto(LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                SELECT p.codigo_barras, p.nombre,
                       COALESCE(SUM(dv.total_linea), 0) AS ventas,
                       COALESCE(SUM(dv.cantidad * p.costo), 0) AS costo_venta,
                       COALESCE(SUM(dv.total_linea), 0) - COALESCE(SUM(dv.cantidad * p.costo), 0) AS utilidad_bruta
                FROM detalle_venta dv
                JOIN ventas v ON v.id = dv.venta_id
                JOIN productos p ON p.id = dv.producto_id
                WHERE v.estado = 'CONFIRMADA' AND v.fecha::date BETWEEN :desde AND :hasta
                GROUP BY p.codigo_barras, p.nombre
                ORDER BY utilidad_bruta DESC
                """)
                .param("desde", desde)
                .param("hasta", hasta)
                .query(this::mapRow).list();
    }

    public List<Map<String, Object>> valorInventario() {
        return jdbc.sql("""
                SELECT p.codigo_barras, p.nombre, c.nombre AS categoria,
                       p.stock, p.costo, (p.stock * p.costo) AS valor
                FROM productos p
                LEFT JOIN categorias c ON c.id = p.categoria_id
                WHERE p.activo
                ORDER BY c.nombre, p.nombre
                """).query(this::mapRow).list();
    }

    public List<Map<String, Object>> cierresCaja(LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                SELECT c.id, u.nombre AS cajero, c.estado, c.base_efectivo, c.apertura, c.cierre,
                       c.efectivo_contado, c.diferencia
                FROM cajas c
                JOIN usuarios u ON u.id = c.usuario_id
                WHERE c.estado = 'CERRADA'
                  AND c.cierre::date BETWEEN :desde AND :hasta
                ORDER BY c.cierre DESC
                """)
                .param("desde", desde)
                .param("hasta", hasta)
                .query(this::mapRow).list();
    }

    public Map<String, Object> panel() {
        Map<String, Object> res = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();
        List<Map<String, Object>> ventasHoy = jdbc.sql("""
                SELECT COALESCE(SUM(total), 0) AS total, COUNT(*) AS numero
                FROM ventas
                WHERE estado = 'CONFIRMADA' AND fecha::date = :hoy
                """).param("hoy", hoy).query(this::mapRow).list();
        res.put("ventasHoy", ventasHoy.isEmpty() ? Map.of() : ventasHoy.get(0));
        List<Map<String, Object>> stock = jdbc.sql("""
                SELECT estado, COUNT(*) AS cantidad
                FROM v_estado_stock
                GROUP BY estado
                """).query(this::mapRow).list();
        res.put("stock", stock);
        return res;
    }

    public DatosReporte datosParaExportar(String reporte, LocalDate desde, LocalDate hasta) {
        return switch (reporte) {
            case "stock-bajo" -> new DatosReporte("stock_bajo", stockBajo());
            case "agotados" -> new DatosReporte("agotados", agotados());
            case "por-agotarse" -> new DatosReporte("por_agotarse", porAgotarse());
            case "pedido-proveedor" -> new DatosReporte("pedido_proveedor", pedidoProveedor());
            case "productos-mas-vendidos" -> new DatosReporte("productos_mas_vendidos",
                    productosMasVendidos(desde, hasta, 20));
            case "productos-sin-movimiento" -> new DatosReporte("productos_sin_movimiento",
                    productosSinMovimiento(60));
            case "valor-inventario" -> new DatosReporte("valor_inventario", valorInventario());
            case "cierres-caja" -> new DatosReporte("cierres_caja", cierresCaja(desde, hasta));
            default -> new DatosReporte(reporte, List.of());
        };
    }

    public record DatosReporte(String nombre, List<Map<String, Object>> filas) {
    }

    private Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        int cols = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String label = rs.getMetaData().getColumnLabel(i);
            row.put(label, rs.getObject(i));
        }
        return row;
    }
}
