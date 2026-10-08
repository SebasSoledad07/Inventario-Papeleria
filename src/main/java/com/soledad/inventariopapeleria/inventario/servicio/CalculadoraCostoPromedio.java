package com.soledad.inventariopapeleria.inventario.servicio;

import com.soledad.inventariopapeleria.compartido.Pesos;
import java.math.BigDecimal;

/**
 * Calculadora de costo promedio ponderado (RN-23). Clase pura.
 */
public final class CalculadoraCostoPromedio {

    private CalculadoraCostoPromedio() {
    }

    /**
     * Calcula nuevo costo promedio ponderado.
     * @param stockActual stock actual antes de la entrada
     * @param costoActual costo promedio actual
     * @param cantidadEntrada cantidad que entra
     * @param costoEntrada costo unitario de entrada
     * @return nuevo costo promedio redondeado al peso
     */
    public static BigDecimal calcular(BigDecimal stockActual, BigDecimal costoActual,
                                     BigDecimal cantidadEntrada, BigDecimal costoEntrada) {
        BigDecimal valorActual = stockActual.multiply(costoActual);
        BigDecimal valorEntrada = cantidadEntrada.multiply(costoEntrada);
        BigDecimal nuevoStock = stockActual.add(cantidadEntrada);
        if (nuevoStock.compareTo(BigDecimal.ZERO) <= 0) {
            return Pesos.redondear(costoEntrada);
        }
        BigDecimal nuevoCosto = valorActual.add(valorEntrada).divide(nuevoStock, 0, java.math.RoundingMode.HALF_UP);
        return nuevoCosto;
    }

    // Sobrecarga para ints
    public static BigDecimal calcular(int stockActual, BigDecimal costoActual,
                                     int cantidadEntrada, BigDecimal costoEntrada) {
        return calcular(BigDecimal.valueOf(stockActual), costoActual,
                BigDecimal.valueOf(cantidadEntrada), costoEntrada);
    }
}