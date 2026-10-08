package com.soledad.inventariopapeleria.producto.api;

import com.soledad.inventariopapeleria.producto.dominio.Categoria;

/**
 * Categoria lista para exponer.
 */
public record CategoriaDto(Long id, String nombre, boolean activo) {

    public static CategoriaDto desde(Categoria categoria) {
        return new CategoriaDto(categoria.getId(), categoria.getNombre(), categoria.isActivo());
    }

}