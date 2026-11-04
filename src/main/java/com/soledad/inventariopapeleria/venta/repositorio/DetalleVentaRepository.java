package com.soledad.inventariopapeleria.venta.repositorio;

import com.soledad.inventariopapeleria.venta.dominio.DetalleVenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaId(Long ventaId);
}