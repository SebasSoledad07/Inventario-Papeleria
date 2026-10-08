package com.soledad.inventariopapeleria.unitarias.codigo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.soledad.inventariopapeleria.codigo.GeneradorCodigoInterno;
import com.soledad.inventariopapeleria.codigo.NormalizadorCodigo;
import org.junit.jupiter.api.Test;

/**
 * RN-14: normalizacion de codigos (UPC-A -> EAN-13).
 * RN-15: codigo interno EAN-13 con prefijo 2 + digito de control.
 */
class CodigoTest {

    @Test
    void normalizaEliminaEspacios() {
        assertThat(NormalizadorCodigo.normalizar("  770123 456789 ")).isEqualTo("0770123456789");
    }

    @Test
    void upcAConvierteAEan13() {
        // Ejemplo tipico: 12 digitos -> añade 0 al inicio
        String upc = "770123456789";
        assertThat(NormalizadorCodigo.normalizar(upc).length()).isEqualTo(13);
        assertThat(NormalizadorCodigo.normalizar(upc)).startsWith("0");
    }

    @Test
    void codigoInternoGeneraEan13ConPrefijo2() {
        String interno = GeneradorCodigoInterno.generar("12345");
        assertThat(interno.length()).isEqualTo(13);
        assertThat(interno).startsWith("2");
    }

    @Test
    void codigoInternoCalculaDigitoControlValido() {
        String interno = GeneradorCodigoInterno.generar("1001");
        assertThat(digitoControl(interno.substring(0, 12))).isEqualTo(Character.getNumericValue(interno.charAt(12)));
    }

    @Test
    void codigoInternoRequiereBase() {
        assertThatThrownBy(() -> GeneradorCodigoInterno.generar("")).isInstanceOf(IllegalArgumentException.class);
    }

    private int digitoControl(String primeros12) {
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int d = Character.getNumericValue(primeros12.charAt(i));
            if ((i % 2) == 0) {
                suma += d;
            } else {
                suma += d * 3;
            }
        }
        return (10 - (suma % 10)) % 10;
    }
}