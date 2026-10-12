package com.soledad.inventariopapeleria.inventario.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Conteo fisico en una sola sesion (RF-18, D-3).
 */
public record ConteoFisicoRequest(
        @NotEmpty(message = "El conteo debe traer al menos un producto.")
        @Valid
        List<LineaConteo> lineas) {
}