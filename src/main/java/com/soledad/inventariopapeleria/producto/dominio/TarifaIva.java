package com.soledad.inventariopapeleria.producto.dominio;

/**
 * Tarifa de IVA (RN-09). Precio de venta incluye IVA (RN-04).
 */
public enum TarifaIva {
    IVA_19("19"),
    IVA_5("5"),
    IVA_0("0"),
    EXCLUIDO("EXCLUIDO");

    private final String valor;

    TarifaIva(String valor) {
        this.valor = valor;
    }

    public String valor() {
        return valor;
    }

    public static TarifaIva desde(String valor) {
        if (valor == null) {
            return EXCLUIDO;
        }
        String v = valor.trim();
        for (TarifaIva t : values()) {
            if (t.valor.equals(v) || t.name().equals(v)) {
                return t;
            }
        }
        return EXCLUIDO;
    }
}