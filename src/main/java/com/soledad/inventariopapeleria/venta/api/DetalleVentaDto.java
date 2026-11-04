package com.soledad.inventariopapeleria.venta.api;

import java.math.BigDecimal;

public record DetalleVentaDto(Long id, Long productoId, String productoNombre, Integer cantidad,
                              BigDecimal precioUnitario, BigDecimal base, BigDecimal iva,
                              BigDecimal descuento, BigDecimal totalLinea) {
}