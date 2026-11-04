package com.soledad.inventariopapeleria.venta.repositorio;

import com.soledad.inventariopapeleria.venta.dominio.Caja;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    Optional<Caja> findFirstByUsuarioIdAndEstadoOrderByAperturaDesc(Long usuarioId, String estado);

    List<Caja> findByUsuarioIdOrderByAperturaDesc(Long usuarioId);
}