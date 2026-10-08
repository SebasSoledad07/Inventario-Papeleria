package com.soledad.inventariopapeleria.configuracion.api;

import com.soledad.inventariopapeleria.configuracion.dominio.Configuracion;

/**
 * Parametro del negocio (RF-45, RF-46).
 */
public record ConfiguracionDto(String clave, String valor, String descripcion) {

    public static ConfiguracionDto desde(Configuracion configuracion) {
        return new ConfiguracionDto(configuracion.getClave(), configuracion.getValor(),
                configuracion.getDescripcion());
    }

}