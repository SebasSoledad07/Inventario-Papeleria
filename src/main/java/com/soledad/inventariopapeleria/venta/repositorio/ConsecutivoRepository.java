package com.soledad.inventariopapeleria.venta.repositorio;

import com.soledad.inventariopapeleria.venta.dominio.Consecutivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsecutivoRepository extends JpaRepository<Consecutivo, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE consecutivos SET ultimo = ultimo + 1 WHERE nombre = 'VENTA' RETURNING ultimo", nativeQuery = true)
    Integer obtenerSiguienteConsecutivoVenta();
}