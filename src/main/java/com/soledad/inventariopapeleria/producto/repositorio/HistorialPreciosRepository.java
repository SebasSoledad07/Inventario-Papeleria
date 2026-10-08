package com.soledad.inventariopapeleria.producto.repositorio;

import com.soledad.inventariopapeleria.producto.dominio.HistorialPrecios;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialPreciosRepository extends JpaRepository<HistorialPrecios, Long> {
}