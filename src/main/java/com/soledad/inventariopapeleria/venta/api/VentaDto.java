package com.soledad.inventariopapeleria.venta.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VentaDto(Long id, Integer consecutivo, Long cajaId, Long usuarioId, String usuarioNombre,
                       Long clienteId, String clienteNombre, Instant fecha, String estado,
                       BigDecimal subtotal, BigDecimal descuentoTotal, BigDecimal ivaTotal,
                       BigDecimal total, BigDecimal cambio, String motivoAnulacion,
                       Long descuentoAutorizadoPor, List<DetalleVentaDto> detalle, List<PagoDto> pagos) {

    public static VentaDto desde(com.soledad.inventariopapeleria.venta.dominio.Venta venta,
                                List<com.soledad.inventariopapeleria.venta.dominio.DetalleVenta> detalles,
                                List<com.soledad.inventariopapeleria.venta.dominio.PagoVenta> pagos) {
        List<DetalleVentaDto> lineas = detalles.stream().map(d -> new DetalleVentaDto(
                d.getId(), d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
                d.getPrecioUnitario(), d.getBase(), d.getIva(), d.getDescuento(), d.getTotalLinea())).toList();
        List<PagoDto> pagosDto = pagos.stream().map(p -> new PagoDto(p.getId(), p.getMedio(), p.getValor())).toList();
        return new VentaDto(venta.getId(), venta.getConsecutivo(), venta.getCaja().getId(),
                venta.getUsuario().getId(), venta.getUsuario().getNombre(),
                venta.getCliente() != null ? venta.getCliente().getId() : null,
                venta.getCliente() != null ? venta.getCliente().getNombre() : null,
                venta.getFecha(), venta.getEstado(), venta.getSubtotal(), venta.getDescuentoTotal(),
                venta.getIvaTotal(), venta.getTotal(), venta.getCambio(), venta.getMotivoAnulacion(),
                venta.getDescuentoAutorizadoPor(), lineas, pagosDto);
    }
}