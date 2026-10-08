package com.soledad.inventariopapeleria.inventario.repositorio;

import com.soledad.inventariopapeleria.producto.dominio.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Producto, Long> {

    /**
     * Descuenta stock de forma segura (RN-19): solo actualiza si hay unidades
     * suficientes. Devuelve el numero de filas afectadas.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad WHERE p.id = :id AND p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Producto p SET p.stock = p.stock + :cantidad WHERE p.id = :id")
    int incrementarStock(@Param("id") Long id, @Param("cantidad") int cantidad);
}