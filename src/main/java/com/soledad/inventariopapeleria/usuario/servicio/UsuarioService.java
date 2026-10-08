package com.soledad.inventariopapeleria.usuario.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.usuario.api.ActualizarUsuarioRequest;
import com.soledad.inventariopapeleria.usuario.api.CrearUsuarioRequest;
import com.soledad.inventariopapeleria.usuario.api.UsuarioDto;
import com.soledad.inventariopapeleria.usuario.dominio.Rol;
import com.soledad.inventariopapeleria.usuario.dominio.Usuario;
import com.soledad.inventariopapeleria.usuario.repositorio.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio de los usuarios (RF-43, RF-44, RN-43, RN-45).
 */
@Service
public class UsuarioService {

    private final UsuarioRepository repositorio;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoria;

    public UsuarioService(UsuarioRepository repositorio, PasswordEncoder passwordEncoder,
                          AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
        this.auditoria = auditoria;
    }

    @Transactional
    public UsuarioDto crear(CrearUsuarioRequest request, Long actorId) {
        repositorio.findByUsuario(request.usuario().trim())
                .ifPresent(u -> {
                    throw new ErrorNegocio("Ya existe un usuario con el nombre '" + request.usuario() + "'.");
                });

        Usuario usuario = new Usuario();
        usuario.setUsuario(request.usuario().trim());
        usuario.setNombre(request.nombre().trim());
        usuario.setRol(rolDe(request.rol()));
        // RN-43: la clave se guarda cifrada con BCrypt
        usuario.setClave(passwordEncoder.encode(request.clave()));
        Instant ahora = Instant.now();
        usuario.setCreadoEn(ahora);
        usuario.setActualizadoEn(ahora);

        // RN-45: la creacion de usuarios es una accion sensible
        auditoria.registrar(actorId, "CREAR_USUARIO", "Se creo el usuario '" + usuario.getUsuario() + "'.");
        return UsuarioDto.desde(repositorio.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return repositorio.findAllByOrderByNombreAsc().stream()
                .map(UsuarioDto::desde)
                .toList();
    }

    @Transactional
    public UsuarioDto actualizar(Long id, ActualizarUsuarioRequest request, Long actorId) {
        Usuario usuario = buscar(id);
        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.setNombre(request.nombre().trim());
        }
        if (request.rol() != null && !request.rol().isBlank()) {
            usuario.setRol(rolDe(request.rol()));
        }
        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }
        usuario.setActualizadoEn(Instant.now());
        auditoria.registrar(actorId, "EDITAR_USUARIO", "Se edito el usuario '" + usuario.getUsuario() + "'.");
        return UsuarioDto.desde(repositorio.save(usuario));
    }

    @Transactional
    public void inactivar(Long id, Long actorId) {
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        usuario.setActualizadoEn(Instant.now());
        auditoria.registrar(actorId, "INACTIVAR_USUARIO", "Se inactivo el usuario '" + usuario.getUsuario() + "'.");
        repositorio.save(usuario);
    }

    @Transactional
    public void cambiarClave(Long id, String claveNueva, Long actorId) {
        Usuario usuario = buscar(id);
        // RN-43: cifrar la clave antes de guardar
        usuario.setClave(passwordEncoder.encode(claveNueva));
        usuario.setActualizadoEn(Instant.now());
        auditoria.registrar(actorId, "CAMBIAR_CLAVE",
                "Se cambio la clave del usuario '" + usuario.getUsuario() + "'.");
        repositorio.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe el usuario con id " + id + "."));
    }

    private Rol rolDe(String rol) {
        try {
            return Rol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ErrorNegocio("El rol '" + rol + "' no es valido.");
        }
    }

}