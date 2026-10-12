package com.soledad.inventariopapeleria.inventario.api;

import com.soledad.inventariopapeleria.inventario.dominio.MovimientoInventario;
import java.time.Instant;

/**
 * Movimiento del kardex de un producto (RF-17).
 */
public record MovimientoDto(Long id, Long productoId, String tipo, Integer cantidad, Integer stockResultante,
                            String referencia, String motivo, Long usuarioId, String usuarioNombre, Instant fecha) {

    public static MovimientoDto desde(MovimientoInventario movimiento, String usuarioNombre) {
        return new MovimientoDto(movimiento.getId(), movimiento.getProducto().getId(),
                movimiento.getTipo().name(), movimiento.getCantidad(), movimiento.getStockResultante(),
                movimiento.getReferencia(), movimiento.getMotivo(),
                movimiento.getUsuarioId(), usuarioNombre, movimiento.getFecha());
    }
}