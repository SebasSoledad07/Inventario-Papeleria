package com.soledad.inventariopapeleria.venta.api;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CajaRequest(
        @NotNull(message = "La base inicial en efectivo es obligatoria.")
        BigDecimal baseEfectivo) {
}