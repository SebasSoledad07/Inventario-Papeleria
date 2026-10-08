package com.soledad.inventariopapeleria.usuario.seguridad;

import com.soledad.inventariopapeleria.usuario.dominio.Rol;
import com.soledad.inventariopapeleria.usuario.dominio.Usuario;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adapta un {@link Usuario} a {@link UserDetails}. Nunca expone la clave
 * fuera de Spring Security.
 */
public class UsuarioDatos implements UserDetails {

    private final Long id;
    private final String usuario;
    private final String clave;
    private final String nombre;
    private final Rol rol;
    private final boolean activo;

    public UsuarioDatos(Usuario usuario) {
        this.id = usuario.getId();
        this.usuario = usuario.getUsuario();
        this.clave = usuario.getClave();
        this.nombre = usuario.getNombre();
        this.rol = usuario.getRol();
        this.activo = usuario.isActivo();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return usuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

}