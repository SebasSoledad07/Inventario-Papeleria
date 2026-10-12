package com.soledad.inventariopapeleria.inventario.api;

import com.soledad.inventariopapeleria.inventario.servicio.ConteoFisicoService;
import com.soledad.inventariopapeleria.inventario.servicio.InventarioService;
import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService servicio;
    private final ConteoFisicoService conteoFisico;

    public InventarioController(InventarioService servicio, ConteoFisicoService conteoFisico) {
        this.servicio = servicio;
        this.conteoFisico = conteoFisico;
    }

    @PostMapping("/{id}/ajuste-positivo")
    public void ajustePositivo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int cantidad = Integer.parseInt(body.get("cantidad").toString());
        String motivo = body.getOrDefault("motivo", "").toString();
        servicio.ajustarPositivo(id, cantidad, motivo, actor());
    }

    @PostMapping("/{id}/ajuste-negativo")
    public void ajusteNegativo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int cantidad = Integer.parseInt(body.get("cantidad").toString());
        String motivo = body.getOrDefault("motivo", "").toString();
        servicio.ajustarNegativo(id, cantidad, motivo, actor());
    }

    @GetMapping("/productos/{id}/kardex")
    public List<MovimientoDto> kardex(@PathVariable Long id) {
        return servicio.consultarKardex(id);
    }

    @PostMapping("/conteo-fisico")
    public ConteoFisicoResultado conteoFisico(@Valid @RequestBody ConteoFisicoRequest request) {
        return conteoFisico.ejecutar(request, actor());
    }

    private Long actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDatos d) {
            return d.getId();
        }
        return null;
    }
}