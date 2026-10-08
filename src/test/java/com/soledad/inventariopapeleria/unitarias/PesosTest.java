package com.soledad.inventariopapeleria.unitarias;

import static org.assertj.core.api.Assertions.assertThat;

import com.soledad.inventariopapeleria.compartido.Pesos;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Tabla de casos para el formato y el redondeo del dinero (RN-02, RN-03).
 */
class PesosTest {

    @Test
    void redondeaAlPesoMasCercano() {
        assertThat(Pesos.redondear(new BigDecimal("4.4"))).isEqualByComparingTo("4");
        assertThat(Pesos.redondear(new BigDecimal("4.5"))).isEqualByComparingTo("5");
        assertThat(Pesos.redondear(new BigDecimal("0.49"))).isEqualByComparingTo("0");
    }

    @Test
    void redondearNuncaDevuelveDecimales() {
        assertThat(Pesos.redondear(new BigDecimal("1234.567")).scale()).isZero();
    }

    @Test
    void formateaEnPesosColombianosSinDecimales() {
        assertThat(Pesos.formatear(new BigDecimal("4500"))).contains("4.500");
        assertThat(Pesos.formatear(new BigDecimal("125000"))).contains("125.000");
        assertThat(Pesos.formatear(new BigDecimal("4500"))).doesNotContain(",");
    }

    @Test
    void formateaEnteroLargo() {
        assertThat(Pesos.formatear(125000L)).contains("125.000");
    }

}