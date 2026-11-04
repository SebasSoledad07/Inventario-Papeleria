package com.soledad.inventariopapeleria.venta.api;

import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import com.soledad.inventariopapeleria.venta.servicio.TirillaService;
import com.soledad.inventariopapeleria.venta.servicio.VentaService;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venta")
public class VentaController {

    private final VentaService servicio;
    private final TirillaService tirilla;

    public VentaController(VentaService servicio, TirillaService tirilla) {
        this.servicio = servicio;
        this.tirilla = tirilla;
    }

    @PostMapping
    public VentaDto confirmar(@Valid @RequestBody VentaRequest req) {
        return servicio.confirmar(req, actor());
    }

    @GetMapping("/{id}")
    public VentaDto consultar(@PathVariable Long id) {
        return servicio.consultar(id);
    }

    @GetMapping
    public java.util.List<VentaDto> buscar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(required = false) Long cajeroId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Integer consecutivo) {
        return servicio.buscar(desde, hasta, cajeroId, clienteId, consecutivo);
    }

    @PostMapping("/{id}/anular")
    public VentaDto anular(@PathVariable Long id, @Valid @RequestBody AnulacionRequest req) {
        return servicio.anular(id, req, actor());
    }

    @GetMapping(value = "/{id}/tirilla", produces = MediaType.TEXT_HTML_VALUE)
    public String tirilla(@PathVariable Long id) {
        return tirilla.generar(id);
    }

    private Long actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDatos d) {
            return d.getId();
        }
        return null;
    }
}