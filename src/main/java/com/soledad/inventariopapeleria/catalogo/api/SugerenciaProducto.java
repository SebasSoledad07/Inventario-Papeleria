package com.soledad.inventariopapeleria.catalogo.api;

import jakarta.validation.constraints.NotBlank;

public record SugerenciaProducto(String codigo, String nombre, String marca, String categoria) {
}