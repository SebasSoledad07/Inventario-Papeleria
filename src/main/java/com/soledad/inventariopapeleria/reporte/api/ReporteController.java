package com.soledad.inventariopapeleria.reporte.api;

import com.soledad.inventariopapeleria.reporte.exportacion.ExportadorExcel;
import com.soledad.inventariopapeleria.reporte.exportacion.ExportadorPdf;
import com.soledad.inventariopapeleria.reporte.servicio.ReporteService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reporte")
public class ReporteController {

    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/stock-bajo")
    public Object stockBajo() { return servicio.stockBajo(); }

    @GetMapping("/agotados")
    public Object agotados() { return servicio.agotados(); }

    @GetMapping("/por-agotarse")
    public Object porAgotarse() { return servicio.porAgotarse(); }

    @GetMapping("/pedido-proveedor")
    public Object pedidoProveedor() { return servicio.pedidoProveedor(); }

    @GetMapping("/ventas-periodo")
    public Object ventasPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "dia") String agrupacion) {
        return servicio.ventasPeriodo(desde, hasta, agrupacion);
    }

    @GetMapping("/productos-mas-vendidos")
    public Object productosMasVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limite) {
        return servicio.productosMasVendidos(desde, hasta, limite);
    }

    @GetMapping("/productos-sin-movimiento")
    public Object productosSinMovimiento(@RequestParam(defaultValue = "60") int dias) {
        return servicio.productosSinMovimiento(dias);
    }

    @GetMapping("/utilidad-periodo")
    public Object utilidadPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.utilidadPeriodo(desde, hasta);
    }

    @GetMapping("/utilidad-categoria")
    public Object utilidadCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.utilidadCategoria(desde, hasta);
    }

    @GetMapping("/utilidad-producto")
    public Object utilidadProducto(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.utilidadProducto(desde, hasta);
    }

    @GetMapping("/valor-inventario")
    public Object valorInventario() { return servicio.valorInventario(); }

    @GetMapping("/cierres-caja")
    public Object cierresCaja(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.cierresCaja(desde, hasta);
    }

    @GetMapping("/panel")
    public Object panel() { return servicio.panel(); }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestParam String reporte,
            @RequestParam(defaultValue = "excel") String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        var datos = servicio.datosParaExportar(reporte, desde == null ? LocalDate.now().minusDays(30) : desde,
                hasta == null ? LocalDate.now() : hasta);
        byte[] bytes;
        String ct;
        String ext;
        if ("pdf".equalsIgnoreCase(formato)) {
            bytes = ExportadorPdf.exportar(datos.nombre(), datos.filas());
            ct = "application/pdf";
            ext = "pdf";
        } else {
            bytes = ExportadorExcel.exportar(datos.nombre(), datos.filas());
            ct = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            ext = "xlsx";
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + datos.nombre() + "." + ext)
                .contentType(MediaType.parseMediaType(ct))
                .body(bytes);
    }
}