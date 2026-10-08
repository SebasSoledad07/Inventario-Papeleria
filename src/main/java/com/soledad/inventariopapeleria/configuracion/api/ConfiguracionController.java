package com.soledad.inventariopapeleria.configuracion.api;

import com.soledad.inventariopapeleria.configuracion.servicio.ConfiguracionService;
import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuracion del negocio (RF-45) y parametros (RF-46). Solo ADMIN.
 */
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService servicio;

    public ConfiguracionController(ConfiguracionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ConfiguracionDto> listar() {
        return servicio.listar();
    }

    @PutMapping
    public List<ConfiguracionDto> actualizar(@RequestBody Map<String, String> valores) {
        Long actorId = ((UsuarioDatos) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return servicio.actualizar(valores, actorId);
    }

}