package com.soledad.inventariopapeleria.error;

/**
 * Recurso solicitado que no existe. Se traduce a 404 en el
 * {@link GlobalHandler}.
 */
public class RecursoNoEncontrado extends ErrorNegocio {

    public RecursoNoEncontrado(String mensaje) {
        super(mensaje);
    }

}