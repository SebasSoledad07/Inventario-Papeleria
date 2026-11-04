package com.soledad.inventariopapeleria.venta.repositorio;

import com.soledad.inventariopapeleria.venta.dominio.Venta;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByConsecutivo(Integer consecutivo);

    List<Venta> findByCajaId(Long cajaId);

    @Query("SELECT v FROM Venta v WHERE (:desde IS NULL OR v.fecha >= :desde) "
            + "AND (:hasta IS NULL OR v.fecha <= :hasta) "
            + "AND (:cajeroId IS NULL OR v.usuario.id = :cajeroId) "
            + "AND (:clienteId IS NULL OR v.cliente.id = :clienteId) "
            + "AND (:consecutivo IS NULL OR v.consecutivo = :consecutivo) "
            + "ORDER BY v.fecha DESC")
    List<Venta> buscar(@Param("desde") Instant desde,
                       @Param("hasta") Instant hasta,
                       @Param("cajeroId") Long cajeroId,
                       @Param("clienteId") Long clienteId,
                       @Param("consecutivo") Integer consecutivo);
}