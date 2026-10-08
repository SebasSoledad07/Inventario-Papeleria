package com.soledad.inventariopapeleria.usuario.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un usuario (RF-44).
 */
public record CrearUsuarioRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(max = 60, message = "El nombre de usuario no puede superar 60 caracteres.")
        String usuario,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
        String nombre,

        @NotBlank(message = "La clave es obligatoria.")
        @Size(min = 8, max = 100, message = "La clave debe tener entre 8 y 100 caracteres.")
        String clave,

        @NotBlank(message = "El rol es obligatorio.")
        String rol) {
}