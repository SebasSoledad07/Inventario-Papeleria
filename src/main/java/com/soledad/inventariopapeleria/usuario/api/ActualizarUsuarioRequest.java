package com.soledad.inventariopapeleria.usuario.api;

/**
 * Datos para editar un usuario (RF-44).
 */
public record ActualizarUsuarioRequest(String nombre, String rol, Boolean activo) {
}