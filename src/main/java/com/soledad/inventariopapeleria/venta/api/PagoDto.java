package com.soledad.inventariopapeleria.venta.api;

import java.math.BigDecimal;

public record PagoDto(Long id, String medio, BigDecimal valor) {
}