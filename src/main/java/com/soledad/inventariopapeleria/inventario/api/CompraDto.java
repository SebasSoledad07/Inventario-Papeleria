package com.soledad.inventariopapeleria.inventario.api;

import com.soledad.inventariopapeleria.inventario.dominio.Compra;
import com.soledad.inventariopapeleria.inventario.dominio.DetalleCompra;
import java.time.Instant;
import java.util.List;

/**
 * Compra lista para exponer.
 */
public record CompraDto(Long id, Long proveedorId, String proveedorRazonSocial, String numeroFactura,
                        Instant fecha, String observacion, List<DetalleCompraDto> detalle) {

    public static CompraDto desde(Compra compra, List<DetalleCompra> detalles) {
        List<DetalleCompraDto> lineas = detalles.stream().map(DetalleCompraDto::desde).toList();
        return new CompraDto(compra.getId(), compra.getProveedor().getId(),
                compra.getProveedor().getRazonSocial(), compra.getNumeroFactura(),
                compra.getFecha(), compra.getObservacion(), lineas);
    }
}