package com.soledad.inventariopapeleria.compartido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilidades de dinero. El dinero se maneja en pesos enteros (RN-02),
 * nunca con double ni float.
 */
public final class Pesos {

    private static final NumberFormat FORMATO_COP =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    static {
        FORMATO_COP.setMaximumFractionDigits(0);
    }

    private Pesos() {
    }

    /** Redondea al peso mas cercano (RN-02). */
    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP);
    }

    /** Formato colombiano, ej. $ 4.500 (RN-03). */
    public static String formatear(BigDecimal valor) {
        return FORMATO_COP.format(valor);
    }

    /** Formato colombiano a partir del valor redondeado (RN-03). */
    public static String formatear(long valor) {
        return FORMATO_COP.format(valor);
    }

}