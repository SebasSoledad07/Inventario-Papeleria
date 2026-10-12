package com.soledad.inventariopapeleria.inventario.repositorio;

import com.soledad.inventariopapeleria.inventario.dominio.DetalleCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    List<DetalleCompra> findByCompraId(Long compraId);
}