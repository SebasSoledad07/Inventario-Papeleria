package com.soledad.inventariopapeleria.inventario.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Linea de una entrada de mercancia (RF-14).
 */
public record DetalleCompraRequest(
        @NotNull(message = "El producto es obligatorio.")
        Long productoId,

        @Min(value = 1, message = "La cantidad debe ser mayor a cero.")
        int cantidad,

        @NotNull(message = "El costo unitario es obligatorio.")
        @PositiveOrZero(message = "El costo unitario no puede ser negativo.")
        BigDecimal costoUnitario) {
}