package com.soledad.inventariopapeleria.venta.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record VentaRequest(
        Long cajaId,
        Long clienteId,
        BigDecimal descuentoTotal,
        Long descuentoAutorizadoPor,
        @NotEmpty(message = "La venta debe traer al menos un producto.")
        @Valid
        List<ItemVentaRequest> items,
        @NotEmpty(message = "La venta debe traer al menos un pago.")
        @Valid
        List<PagoRequest> pagos) {
}