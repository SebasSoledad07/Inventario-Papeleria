package com.soledad.inventariopapeleria.cliente.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "El tipo de documento es obligatorio.")
        String tipoDocumento,
        @NotBlank(message = "El numero de documento es obligatorio.")
        @Size(max = 20)
        String numeroDocumento,
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150)
        String nombre,
        @Size(max = 30)
        String telefono) {
}