package com.soledad.inventariopapeleria.producto.repositorio;

import com.soledad.inventariopapeleria.producto.dominio.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);

    List<Categoria> findAllByActivoTrueOrderByNombreAsc();

    List<Categoria> findAllByOrderByNombreAsc();

}