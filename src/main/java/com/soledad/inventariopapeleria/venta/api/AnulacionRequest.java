package com.soledad.inventariopapeleria.venta.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnulacionRequest(
        @NotBlank(message = "El motivo de anulacion es obligatorio.")
        @Size(max = 150, message = "El motivo no puede superar 150 caracteres.")
        String motivo) {
}