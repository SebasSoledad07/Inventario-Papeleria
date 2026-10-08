package com.soledad.inventariopapeleria.proveedor.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un proveedor.
 */
public record ProveedorRequest(
        @NotBlank(message = "La razon social es obligatoria.")
        @Size(max = 150, message = "La razon social no puede superar 150 caracteres.")
        String razonSocial,

        @NotBlank(message = "El NIT es obligatorio.")
        @Size(max = 20, message = "El NIT no puede superar 20 caracteres.")
        String nit,

        @Size(max = 30, message = "El telefono no puede superar 30 caracteres.")
        String telefono,

        @Size(max = 150, message = "La direccion no puede superar 150 caracteres.")
        String direccion) {
}