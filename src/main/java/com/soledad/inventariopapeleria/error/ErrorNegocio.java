package com.soledad.inventariopapeleria.error;

/**
 * Error de regla de negocio: se traduce a una respuesta 400 en el
 * {@link GlobalHandler}. Los mensajes van en espanol de Colombia (RNF-03).
 */
public class ErrorNegocio extends RuntimeException {

    public ErrorNegocio(String mensaje) {
        super(mensaje);
    }

    public ErrorNegocio(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

}