package com.soledad.inventariopapeleria.inventario.api;

import com.soledad.inventariopapeleria.inventario.dominio.DetalleCompra;
import java.math.BigDecimal;

/**
 * Linea de compra lista para exponer.
 */
public record DetalleCompraDto(Long productoId, String codigoBarras, String productoNombre,
                               Integer cantidad, BigDecimal costoUnitario) {

    public static DetalleCompraDto desde(DetalleCompra detalle) {
        return new DetalleCompraDto(detalle.getProducto().getId(), detalle.getProducto().getCodigoBarras(),
                detalle.getProducto().getNombre(), detalle.getCantidad(), detalle.getCostoUnitario());
    }
}