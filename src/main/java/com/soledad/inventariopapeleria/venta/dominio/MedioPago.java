package com.soledad.inventariopapeleria.venta.dominio;

/**
 * Medios de pago aceptados (RN-34).
 */
public enum MedioPago {
    EFECTIVO,
    TARJETA_DEBITO,
    TARJETA_CREDITO,
    NEQUI,
    DAVIPLATA,
    TRANSFERENCIA;

    public static MedioPago desde(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Medio de pago obligatorio.");
        }
        return MedioPago.valueOf(valor.trim().toUpperCase());
    }
}