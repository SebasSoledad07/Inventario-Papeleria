package com.soledad.inventariopapeleria.producto.api;

import com.soledad.inventariopapeleria.producto.servicio.CategoriaService;
import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administracion de categorias (RF-08). Leer puede cualquier usuario
 * autenticado; escribir solo ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/categoria")
public class CategoriaController {

    private final CategoriaService servicio;

    public CategoriaController(CategoriaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<CategoriaDto> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return servicio.listar(soloActivas);
    }

    @PostMapping
    public CategoriaDto crear(@Valid @RequestBody CategoriaRequest request) {
        return servicio.crear(request, actorActual());
    }

    @PutMapping("/{id}")
    public CategoriaDto actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return servicio.actualizar(id, request, actorActual());
    }

    @DeleteMapping("/{id}")
    public void inactivar(@PathVariable Long id) {
        servicio.inactivar(id, actorActual());
    }

    private Long actorActual() {
        return ((UsuarioDatos) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

}