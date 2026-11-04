package com.soledad.inventariopapeleria.venta.repositorio;

import com.soledad.inventariopapeleria.venta.dominio.PagoVenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoVentaRepository extends JpaRepository<PagoVenta, Long> {

    List<PagoVenta> findByVentaId(Long ventaId);
}