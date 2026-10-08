package com.soledad.inventariopapeleria.configuracion.repositorio;

import com.soledad.inventariopapeleria.configuracion.dominio.Configuracion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {

    List<Configuracion> findAllByOrderByClaveAsc();

}