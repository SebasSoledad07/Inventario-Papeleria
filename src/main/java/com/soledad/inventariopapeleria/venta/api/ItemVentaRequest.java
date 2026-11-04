package com.soledad.inventariopapeleria.venta.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemVentaRequest(
        @NotNull(message = "El producto es obligatorio.")
        Long productoId,

        @Min(value = 1, message = "La cantidad debe ser mayor a cero.")
        int cantidad,

        BigDecimal descuento) {
}