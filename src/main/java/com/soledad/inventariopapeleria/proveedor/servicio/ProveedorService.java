package com.soledad.inventariopapeleria.proveedor.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.proveedor.api.ProveedorDto;
import com.soledad.inventariopapeleria.proveedor.api.ProveedorRequest;
import com.soledad.inventariopapeleria.proveedor.dominio.Proveedor;
import com.soledad.inventariopapeleria.proveedor.repositorio.ProveedorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio de los proveedores (RF-08, RF-14).
 */
@Service
public class ProveedorService {

    private final ProveedorRepository repositorio;
    private final AuditoriaService auditoria;

    public ProveedorService(ProveedorRepository repositorio, AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    @Transactional
    public ProveedorDto crear(ProveedorRequest request, Long actorId) {
        validarRazonSocial(request.razonSocial());
        validarNit(request.nit());

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(request.razonSocial().trim());
        proveedor.setNit(request.nit().trim());
        proveedor.setTelefono(request.telefono());
        proveedor.setDireccion(request.direccion());
        auditoria.registrar(actorId, "CREAR_PROVEEDOR", "Se creo el proveedor '" + request.razonSocial() + "'.");
        return ProveedorDto.desde(repositorio.save(proveedor));
    }

    @Transactional(readOnly = true)
    public List<ProveedorDto> listar(boolean soloActivos) {
        return (soloActivos ? repositorio.findAllByActivoTrueOrderByRazonSocialAsc()
                : repositorio.findAllByOrderByRazonSocialAsc()).stream()
                .map(ProveedorDto::desde)
                .toList();
    }

    @Transactional
    public ProveedorDto actualizar(Long id, ProveedorRequest request, Long actorId) {
        Proveedor proveedor = buscar(id);
        if (!proveedor.getRazonSocial().equalsIgnoreCase(request.razonSocial())) {
            validarRazonSocial(request.razonSocial());
        }
        if (!proveedor.getNit().equalsIgnoreCase(request.nit())) {
            validarNit(request.nit());
        }
        proveedor.setRazonSocial(request.razonSocial().trim());
        proveedor.setNit(request.nit().trim());
        proveedor.setTelefono(request.telefono());
        proveedor.setDireccion(request.direccion());
        auditoria.registrar(actorId, "EDITAR_PROVEEDOR", "Se edito el proveedor '" + request.razonSocial() + "'.");
        return ProveedorDto.desde(repositorio.save(proveedor));
    }

    @Transactional
    public void inactivar(Long id, Long actorId) {
        Proveedor proveedor = buscar(id);
        proveedor.setActivo(false);
        auditoria.registrar(actorId, "INACTIVAR_PROVEEDOR",
                "Se inactivo el proveedor '" + proveedor.getRazonSocial() + "'.");
        repositorio.save(proveedor);
    }

    private Proveedor buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe el proveedor con id " + id + "."));
    }

    private void validarRazonSocial(String razonSocial) {
        repositorio.findByRazonSocial(razonSocial.trim())
                .ifPresent(p -> {
                    throw new ErrorNegocio("Ya existe un proveedor con la razon social '" + razonSocial + "'.");
                });
    }

    private void validarNit(String nit) {
        repositorio.findByNit(nit.trim())
                .ifPresent(p -> {
                    throw new ErrorNegocio("Ya existe un proveedor con el NIT '" + nit + "'.");
                });
    }

}