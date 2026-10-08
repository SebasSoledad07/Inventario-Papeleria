package com.soledad.inventariopapeleria.inventario.dominio;

/**
 * Tipos de movimiento de inventario (RN-21).
 */
public enum TipoMovimiento {
    ENTRADA_COMPRA,
    SALIDA_VENTA,
    DEVOLUCION_VENTA,
    AJUSTE_POSITIVO,
    AJUSTE_NEGATIVO,
    STOCK_INICIAL
}