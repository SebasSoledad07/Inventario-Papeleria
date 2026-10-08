package com.soledad.inventariopapeleria.inventario.repositorio;

import com.soledad.inventariopapeleria.inventario.dominio.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {
}