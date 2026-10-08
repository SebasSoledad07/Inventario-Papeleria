package com.soledad.inventariopapeleria.usuario.api;

import com.soledad.inventariopapeleria.usuario.dominio.Rol;
import com.soledad.inventariopapeleria.usuario.dominio.Usuario;

/**
 * Usuario listo para exponer: nunca incluye la clave.
 */
public record UsuarioDto(Long id, String usuario, String nombre, Rol rol, boolean activo) {

    public static UsuarioDto desde(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getUsuario(),
                usuario.getNombre(), usuario.getRol(), usuario.isActivo());
    }

}