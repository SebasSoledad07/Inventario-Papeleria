package com.soledad.inventariopapeleria.venta.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PagoRequest(
        @NotBlank(message = "El medio de pago es obligatorio.")
        String medio,

        @NotNull(message = "El valor del pago es obligatorio.")
        BigDecimal valor) {
}