package com.soledad.inventariopapeleria.proveedor.api;

import com.soledad.inventariopapeleria.proveedor.dominio.Proveedor;

/**
 * Proveedor listo para exponer.
 */
public record ProveedorDto(Long id, String razonSocial, String nit, String telefono,
                           String direccion, boolean activo) {

    public static ProveedorDto desde(Proveedor proveedor) {
        return new ProveedorDto(proveedor.getId(), proveedor.getRazonSocial(), proveedor.getNit(),
                proveedor.getTelefono(), proveedor.getDireccion(), proveedor.isActivo());
    }

}