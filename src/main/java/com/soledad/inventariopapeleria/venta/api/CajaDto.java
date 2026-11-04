package com.soledad.inventariopapeleria.venta.api;

import java.math.BigDecimal;
import java.time.Instant;

public record CajaDto(Long id, Long usuarioId, String usuarioNombre, String estado,
                      BigDecimal baseEfectivo, Instant apertura, Instant cierre,
                      BigDecimal efectivoContado, BigDecimal diferencia) {
}