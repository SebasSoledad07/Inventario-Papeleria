package com.soledad.inventariopapeleria.unitarias.inventario;

import static org.assertj.core.api.Assertions.assertThat;

import com.soledad.inventariopapeleria.inventario.servicio.CalculadoraCostoPromedio;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculadoraCostoPromedioTest {

    @Test
    void calculaCostoPromedioPonderado() {
        // stock 10, costo 1000; entra 5 a 1200 -> (10*1000 + 5*1200)/(15)= (10000+6000)/15=1066.66 -> 1067 con HALF_UP
        BigDecimal nuevo = CalculadoraCostoPromedio.calcular(10, new BigDecimal("1000"),
                5, new BigDecimal("1200"));
        assertThat(nuevo).isEqualByComparingTo("1067");
    }

    @Test
    void conEntradaCeroManejaBien() {
        BigDecimal nuevo = CalculadoraCostoPromedio.calcular(5, new BigDecimal("800"),
                0, new BigDecimal("900"));
        assertThat(nuevo).isEqualByComparingTo("800");
    }
}