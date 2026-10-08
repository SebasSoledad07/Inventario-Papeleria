package com.soledad.inventariopapeleria.usuario.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Nueva clave de un usuario.
 */
public record CambiarClaveRequest(
        @NotBlank(message = "La clave es obligatoria.")
        @Size(min = 8, max = 100, message = "La clave debe tener entre 8 y 100 caracteres.")
        String clave) {
}