package com.soledad.inventariopapeleria.unitarias.venta;

import static org.assertj.core.api.Assertions.assertThat;

import com.soledad.inventariopapeleria.venta.servicio.CalculadoraVenta;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * RN-11: precio con IVA incluido, base e IVA redondeados al peso.
 */
class CalculadoraVentaTest {

    @Test
    void calculaIva19() {
        var d = CalculadoraVenta.calcularIvaIncluido(new BigDecimal("11900"), new BigDecimal("19"));
        assertThat(d.base().add(d.iva())).isEqualByComparingTo("11900");
        assertThat(d.base().scale()).isZero();
        assertThat(d.iva().scale()).isZero();
    }

    @Test
    void calculaIva5() {
        var d = CalculadoraVenta.calcularIvaIncluido(new BigDecimal("10500"), new BigDecimal("5"));
        assertThat(d.base()).isEqualByComparingTo("10000");
        assertThat(d.iva()).isEqualByComparingTo("500");
    }
}