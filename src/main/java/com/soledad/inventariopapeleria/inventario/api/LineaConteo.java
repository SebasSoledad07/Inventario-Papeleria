package com.soledad.inventariopapeleria.inventario.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Linea del conteo fisico (RF-18, D-3).
 */
public record LineaConteo(
        @NotNull(message = "El producto es obligatorio.")
        Long productoId,

        @Min(value = 0, message = "La cantidad contada no puede ser negativa.")
        int cantidadContada) {
}