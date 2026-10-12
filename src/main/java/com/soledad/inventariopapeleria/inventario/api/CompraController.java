package com.soledad.inventariopapeleria.inventario.api;

import com.soledad.inventariopapeleria.inventario.servicio.CompraService;
import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entradas de mercancia (RF-14).
 */
@RestController
@RequestMapping("/api/inventario/compras")
public class CompraController {

    private final CompraService servicio;

    public CompraController(CompraService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public CompraDto registrar(@Valid @RequestBody CompraRequest request) {
        return servicio.registrar(request, actor());
    }

    @GetMapping("/{id}")
    public CompraDto consultar(@PathVariable Long id) {
        return servicio.consultar(id);
    }

    private Long actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDatos datos) {
            return datos.getId();
        }
        return null;
    }
}