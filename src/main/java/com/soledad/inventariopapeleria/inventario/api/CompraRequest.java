package com.soledad.inventariopapeleria.inventario.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Datos para registrar una entrada de mercancia (RF-14).
 */
public record CompraRequest(
        @NotNull(message = "El proveedor es obligatorio.")
        Long proveedorId,

        @NotBlank(message = "El numero de factura del proveedor es obligatorio.")
        @Size(max = 30, message = "El numero de factura no puede superar 30 caracteres.")
        String numeroFactura,

        @Size(max = 2000, message = "La observacion es demasiado larga.")
        String observacion,

        @NotEmpty(message = "La compra debe traer al menos un producto.")
        @Valid
        List<DetalleCompraRequest> detalle) {
}