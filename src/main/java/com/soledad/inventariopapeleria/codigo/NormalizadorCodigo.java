package com.soledad.inventariopapeleria.codigo;

import java.util.regex.Pattern;

/**
 * Normalizador de codigos de barras (RN-14).
 * - Elimina espacios y otros caracteres no numericos.
 * - Convierte UPC-A (12 digitos) a EAN-13 anadiendo un '0' al inicio.
 */
public final class NormalizadorCodigo {

    private static final Pattern NO_DIGITOS = Pattern.compile("[^0-9]");

    private NormalizadorCodigo() {
    }

    public static String normalizar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "";
        }
        String limpio = NO_DIGITOS.matcher(codigo).replaceAll("");
        if (limpio.length() == 12) {
            // UPC-A -> EAN-13 (RN-14)
            return "0" + limpio;
        }
        return limpio;
    }
}