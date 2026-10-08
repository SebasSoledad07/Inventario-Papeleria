package com.soledad.inventariopapeleria.usuario.repositorio;

import com.soledad.inventariopapeleria.usuario.dominio.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    Optional<Usuario> findByUsuarioAndActivoTrue(String usuario);

    List<Usuario> findAllByOrderByNombreAsc();

}