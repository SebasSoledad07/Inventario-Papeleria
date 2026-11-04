package com.soledad.inventariopapeleria.venta.api;

import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import com.soledad.inventariopapeleria.venta.servicio.CajaService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    private final CajaService servicio;

    public CajaController(CajaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/abrir")
    public CajaDto abrir(@Valid @RequestBody CajaRequest req) {
        return servicio.abrir(actor(), req.baseEfectivo());
    }

    @GetMapping("/abierta")
    public CajaDto abierta() {
        return servicio.cajaAbierta(actor());
    }

    @GetMapping("/{id}")
    public CajaDto consultar(@PathVariable Long id) {
        return servicio.consultar(id);
    }

    @PostMapping("/{id}/cerrar")
    public CajaDto cerrar(@PathVariable Long id, @Valid @RequestBody CierreCajaRequest req) {
        return servicio.cerrar(id, req.efectivoContado(), actor());
    }

    private Long actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDatos d) {
            return d.getId();
        }
        return null;
    }
}