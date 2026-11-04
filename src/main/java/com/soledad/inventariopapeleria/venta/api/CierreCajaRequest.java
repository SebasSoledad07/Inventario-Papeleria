package com.soledad.inventariopapeleria.venta.api;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CierreCajaRequest(
        @NotNull(message = "El efectivo contado es obligatorio.")
        BigDecimal efectivoContado) {
}