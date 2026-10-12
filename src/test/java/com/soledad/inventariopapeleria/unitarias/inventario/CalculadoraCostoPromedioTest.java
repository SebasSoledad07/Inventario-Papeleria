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

    @Test
    void redondeaMedioPuntoHaciaArriba() {
        // (1*1001 + 1*1000)/2 = 1000.5 -> 1001 con HALF_UP
        BigDecimal nuevo = CalculadoraCostoPromedio.calcular(1, new BigDecimal("1001"),
                1, new BigDecimal("1000"));
        assertThat(nuevo).isEqualByComparingTo("1001");
        assertThat(nuevo.scale()).isZero();
    }

    @Test
    void entradaSobreStockCeroIniciaElCosto() {
        BigDecimal nuevo = CalculadoraCostoPromedio.calcular(0, BigDecimal.ZERO,
                5, new BigDecimal("900"));
        assertThat(nuevo).isEqualByComparingTo("900");
    }
}