package com.soledad.inventariopapeleria.producto.api;

import com.soledad.inventariopapeleria.producto.servicio.ProductoService;
import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/producto")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ProductoDto crear(@Valid @RequestBody ProductoRequest req) {
        return servicio.crear(req, actor());
    }

    @GetMapping("/codigo/{codigo}")
    public ProductoDto porCodigo(@PathVariable String codigo) {
        return servicio.buscarPorCodigo(codigo);
    }

    @GetMapping("/buscar")
    public List<ProductoDto> buscar(@RequestParam String q) {
        return servicio.buscarPorNombre(q);
    }

    @GetMapping
    public List<ProductoDto> listarActivos() {
        return servicio.listarActivos();
    }

    @PutMapping("/{id}/precio")
    public ProductoDto actualizarPrecio(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal nuevo = body.get("precioVenta");
        return servicio.actualizarPrecio(id, nuevo, actor());
    }

    private Long actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDatos d) {
            return d.getId();
        }
        return null;
    }
}