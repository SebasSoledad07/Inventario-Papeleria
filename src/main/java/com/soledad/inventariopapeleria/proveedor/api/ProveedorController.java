package com.soledad.inventariopapeleria.proveedor.api;

import com.soledad.inventariopapeleria.proveedor.servicio.ProveedorService;
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
 * Administracion de proveedores (RF-08). Escribir solo ADMIN.
 */
@RestController
@RequestMapping("/api/proveedor")
public class ProveedorController {

    private final ProveedorService servicio;

    public ProveedorController(ProveedorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ProveedorDto> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return servicio.listar(soloActivos);
    }

    @PostMapping
    public ProveedorDto crear(@Valid @RequestBody ProveedorRequest request) {
        return servicio.crear(request, actorActual());
    }

    @PutMapping("/{id}")
    public ProveedorDto actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
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