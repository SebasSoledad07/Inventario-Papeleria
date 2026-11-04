package com.soledad.inventariopapeleria.venta.servicio;

import com.soledad.inventariopapeleria.venta.api.VentaDto;
import com.soledad.inventariopapeleria.venta.repositorio.DetalleVentaRepository;
import com.soledad.inventariopapeleria.venta.repositorio.PagoVentaRepository;
import com.soledad.inventariopapeleria.venta.repositorio.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera la tirilla de venta en HTML con CSS @page (RF-26, D-5).
 * PENDIENTE-DECISION-5: impresion desde navegador, sin ESC/POS.
 */
@Service
public class TirillaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleRepository;
    private final PagoVentaRepository pagoRepository;

    public TirillaService(VentaRepository ventaRepository,
                          DetalleVentaRepository detalleRepository,
                          PagoVentaRepository pagoRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleRepository = detalleRepository;
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    public String generar(Long ventaId) {
        var venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new com.soledad.inventariopapeleria.error.RecursoNoEncontrado(
                        "No existe la venta con id " + ventaId + "."));
        var detalles = detalleRepository.findByVentaId(ventaId);
        var pagos = pagoRepository.findByVentaId(ventaId);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Tirilla</title>");
        sb.append("<style>@page{size:58mm auto;margin:0}body{font-family:monospace;font-size:10px;width:58mm}</style>");
        sb.append("</head><body>");
        sb.append("<h3>Venta #").append(venta.getConsecutivo()).append("</h3>");
        sb.append("<p>").append(venta.getFecha()).append("</p>");
        sb.append("<hr/>");
        for (var d : detalles) {
            sb.append(d.getProducto().getNombre()).append(" x").append(d.getCantidad())
                    .append(" ").append(d.getTotalLinea()).append("<br/>");
        }
        sb.append("<hr/>");
        sb.append("Subtotal: ").append(venta.getSubtotal()).append("<br/>");
        sb.append("Descuento: ").append(venta.getDescuentoTotal()).append("<br/>");
        sb.append("IVA: ").append(venta.getIvaTotal()).append("<br/>");
        sb.append("TOTAL: ").append(venta.getTotal()).append("<br/>");
        sb.append("Cambio: ").append(venta.getCambio()).append("<br/>");
        sb.append("<hr/>");
        for (var p : pagos) {
            sb.append(p.getMedio()).append(": ").append(p.getValor()).append("<br/>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }
}