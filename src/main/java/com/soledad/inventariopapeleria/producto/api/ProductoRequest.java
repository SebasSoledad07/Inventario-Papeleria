package com.soledad.inventariopapeleria.producto.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        String codigoBarras,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 100) String marca,
        @NotNull Long categoriaId,
        Long proveedorId,
        @NotBlank String unidad,
        @NotBlank String tarifaIva,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal precioVenta,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal costo,
        Integer stockInicial,
        Integer stockMinimo) {
}