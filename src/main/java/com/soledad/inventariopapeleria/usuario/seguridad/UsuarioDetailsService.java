package com.soledad.inventariopapeleria.usuario.seguridad;

import com.soledad.inventariopapeleria.usuario.repositorio.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga un usuario activo por su nombre (RF-43). Los inactivos no pueden
 * iniciar sesion.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repositorio;

    public UsuarioDetailsService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repositorio.findByUsuarioAndActivoTrue(username)
                .map(UsuarioDatos::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contrasena incorrectos."));
    }

}