package com.soledad.inventariopapeleria.error;

import java.time.Instant;

/**
 * Cuerpo uniforme de respuesta de error.
 *
 * @param timestamp momento en que ocurrio el error
 * @param mensaje   mensaje en espanol de Colombia (RNF-03)
 * @param ruta      ruta que genero el error
 */
public record ErrorRespuesta(Instant timestamp, String mensaje, String ruta) {

    public static ErrorRespuesta de(String mensaje, String ruta) {
        return new ErrorRespuesta(Instant.now(), mensaje, ruta);
    }

}