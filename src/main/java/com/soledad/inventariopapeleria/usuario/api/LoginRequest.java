package com.soledad.inventariopapeleria.usuario.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesion (RF-43).
 */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio.")
        String usuario,

        @NotBlank(message = "La clave es obligatoria.")
        String clave) {
}