package com.soledad.inventariopapeleria.inventario.repositorio;

import com.soledad.inventariopapeleria.inventario.dominio.MovimientoInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByProductoIdOrderByFechaAsc(Long productoId);
}