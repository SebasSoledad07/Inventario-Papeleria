package com.soledad.inventariopapeleria.usuario.api;

import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import com.soledad.inventariopapeleria.usuario.servicio.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administracion de usuarios (RF-44). Solo ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return servicio.listar();
    }

    @PostMapping
    public UsuarioDto crear(@Valid @RequestBody CrearUsuarioRequest request) {
        return servicio.crear(request, actorActual());
    }

    @PutMapping("/{id}")
    public UsuarioDto actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest request) {
        return servicio.actualizar(id, request, actorActual());
    }

    @DeleteMapping("/{id}")
    public void inactivar(@PathVariable Long id) {
        servicio.inactivar(id, actorActual());
    }

    @PutMapping("/{id}/clave")
    public void cambiarClave(@PathVariable Long id, @Valid @RequestBody CambiarClaveRequest request) {
        servicio.cambiarClave(id, request.clave(), actorActual());
    }

    private Long actorActual() {
        return ((UsuarioDatos) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

}