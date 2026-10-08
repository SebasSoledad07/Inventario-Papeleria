package com.soledad.inventariopapeleria.codigo;

/**
 * Generador de codigo interno EAN-13 con prefijo '2' (RN-15).
 * GS1 reserva el prefijo 2 para uso interno. Se calcula el digito de control
 * segun el algoritmo EAN-13 para generar un codigo valido.
 */
public final class GeneradorCodigoInterno {

    private GeneradorCodigoInterno() {
    }

    public static String generar(String base) {
        if (base == null || base.isBlank()) {
            throw new IllegalArgumentException("La base para generar el codigo interno no puede estar vacia.");
        }
        String limpio = base.replaceAll("[^0-9]", "");
        StringBuilder sb = new StringBuilder("2");
        sb.append(limpio);
        // Si queda muy corto, completar con ceros hasta tener 12 digitos antes
        // del digito de control (EAN-13 = 12 + 1). Ajustamos a 12 digitos.
        while (sb.length() < 12) {
            sb.insert(sb.length(), "0");
        }
        if (sb.length() > 12) {
            // Truncar para mantener longitud 12 antes del digito de control
            sb.setLength(12);
        }
        sb.append(digitoControl(sb.substring(0, 12)));
        return sb.toString();
    }

    private static char digitoControl(String primeros12) {
        // Algoritmo EAN-13
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(primeros12.charAt(i));
            if ((i % 2) == 0) {
                suma += digito;
            } else {
                suma += digito * 3;
            }
        }
        int control = (10 - (suma % 10)) % 10;
        return Character.forDigit(control, 10);
    }
}