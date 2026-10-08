package com.soledad.inventariopapeleria.inventario.repositorio;

import com.soledad.inventariopapeleria.inventario.dominio.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {
}