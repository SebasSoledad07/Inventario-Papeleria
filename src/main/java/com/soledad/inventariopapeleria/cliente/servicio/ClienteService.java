package com.soledad.inventariopapeleria.cliente.servicio;

import com.soledad.inventariopapeleria.cliente.api.ClienteDto;
import com.soledad.inventariopapeleria.cliente.api.ClienteRequest;
import com.soledad.inventariopapeleria.cliente.dominio.Cliente;
import com.soledad.inventariopapeleria.cliente.repositorio.ClienteRepository;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClienteDto crear(ClienteRequest req) {
        repository.findByTipoDocumentoAndNumeroDocumento(req.tipoDocumento().trim().toUpperCase(),
                req.numeroDocumento().trim())
                .ifPresent(c -> {
                    throw new ErrorNegocio("Ya existe un cliente con ese documento.");
                });
        Cliente c = new Cliente();
        c.setTipoDocumento(req.tipoDocumento().trim().toUpperCase());
        c.setNumeroDocumento(req.numeroDocumento().trim());
        c.setNombre(req.nombre().trim());
        c.setTelefono(req.telefono() != null ? req.telefono().trim() : null);
        return aDto(repository.save(c));
    }

    @Transactional(readOnly = true)
    public ClienteDto consultar(Long id) {
        return repository.findById(id).map(this::aDto)
                .orElseThrow(() -> new com.soledad.inventariopapeleria.error.RecursoNoEncontrado(
                        "No existe el cliente con id " + id + "."));
    }

    private ClienteDto aDto(Cliente c) {
        return new ClienteDto(c.getId(), c.getTipoDocumento(), c.getNumeroDocumento(), c.getNombre(), c.getTelefono());
    }
}