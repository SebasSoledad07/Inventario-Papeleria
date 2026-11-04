package com.soledad.inventariopapeleria.cliente.api;

import com.soledad.inventariopapeleria.cliente.servicio.ClienteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    private final ClienteService servicio;

    public ClienteController(ClienteService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ClienteDto crear(@Valid @RequestBody ClienteRequest req) {
        return servicio.crear(req);
    }

    @GetMapping("/{id}")
    public ClienteDto consultar(@PathVariable Long id) {
        return servicio.consultar(id);
    }
}