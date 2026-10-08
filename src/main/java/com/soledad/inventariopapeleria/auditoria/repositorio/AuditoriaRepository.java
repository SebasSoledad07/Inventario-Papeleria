package com.soledad.inventariopapeleria.auditoria.repositorio;

import com.soledad.inventariopapeleria.auditoria.dominio.Auditoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findTop100ByOrderByFechaDesc();

}