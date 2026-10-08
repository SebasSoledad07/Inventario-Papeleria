package com.soledad.inventariopapeleria.producto.api;

import com.soledad.inventariopapeleria.producto.dominio.Producto;
import java.math.BigDecimal;

public record ProductoDto(Long id, String codigoBarras, String nombre, String marca,
                         Long categoriaId, String categoriaNombre, Long proveedorId, String proveedorNombre,
                         String unidad, String tarifaIva, BigDecimal precioVenta, BigDecimal costo,
                         Integer stock, Integer stockMinimo, boolean activo) {
    public static ProductoDto desde(Producto p) {
        return new ProductoDto(
                p.getId(),
                p.getCodigoBarras(),
                p.getNombre(),
                p.getMarca(),
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre() : null,
                p.getProveedor() != null ? p.getProveedor().getId() : null,
                p.getProveedor() != null ? p.getProveedor().getRazonSocial() : null,
                p.getUnidad() != null ? p.getUnidad().name() : null,
                p.getTarifaIva() != null ? p.getTarifaIva().valor() : null,
                p.getPrecioVenta(),
                p.getCosto(),
                p.getStock(),
                p.getStockMinimo(),
                p.isActivo());
    }
}