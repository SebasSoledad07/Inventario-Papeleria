package com.soledad.inventariopapeleria.venta.servicio;

import com.soledad.inventariopapeleria.compartido.Pesos;
import java.math.BigDecimal;

/**
 * Calculo de IVA cuando el precio incluye IVA (RN-11): base = precio/(1+tarifa),
 * iva = precio - base, ambos redondeados al peso.
 */
public final class CalculadoraVenta {

    private CalculadoraVenta() {
    }

    public record Desglose(BigDecimal base, BigDecimal iva) {
    }

    public static Desglose calcularIvaIncluido(BigDecimal precio, BigDecimal tarifa) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            return new Desglose(BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal unoMasTarifa = BigDecimal.ONE.add(tarifa.divide(BigDecimal.valueOf(100)));
        BigDecimal base = precio.divide(unoMasTarifa, 10, java.math.RoundingMode.HALF_UP);
        BigDecimal baseRedondeada = Pesos.redondear(base);
        BigDecimal ivaRedondeado = Pesos.redondear(precio.subtract(baseRedondeada));
        return new Desglose(baseRedondeada, ivaRedondeado);
    }
}