package com.soledad.inventariopapeleria.cliente.api;

public record ClienteDto(Long id, String tipoDocumento, String numeroDocumento, String nombre, String telefono) {
}