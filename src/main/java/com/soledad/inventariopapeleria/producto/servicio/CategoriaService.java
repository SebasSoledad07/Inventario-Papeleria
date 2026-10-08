package com.soledad.inventariopapeleria.producto.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.producto.api.CategoriaDto;
import com.soledad.inventariopapeleria.producto.api.CategoriaRequest;
import com.soledad.inventariopapeleria.producto.dominio.Categoria;
import com.soledad.inventariopapeleria.producto.repositorio.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio de las categorias (RF-08).
 */
@Service
public class CategoriaService {

    private final CategoriaRepository repositorio;
    private final AuditoriaService auditoria;

    public CategoriaService(CategoriaRepository repositorio, AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    @Transactional
    public CategoriaDto crear(CategoriaRequest request, Long actorId) {
        validarNombreDisponible(request.nombre());
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre().trim());
        auditoria.registrar(actorId, "CREAR_CATEGORIA", "Se creo la categoria '" + request.nombre() + "'.");
        return CategoriaDto.desde(repositorio.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar(boolean soloActivas) {
        return (soloActivas ? repositorio.findAllByActivoTrueOrderByNombreAsc()
                : repositorio.findAllByOrderByNombreAsc()).stream()
                .map(CategoriaDto::desde)
                .toList();
    }

    @Transactional
    public CategoriaDto actualizar(Long id, CategoriaRequest request, Long actorId) {
        Categoria categoria = buscar(id);
        if (!categoria.getNombre().equalsIgnoreCase(request.nombre())) {
            validarNombreDisponible(request.nombre());
        }
        categoria.setNombre(request.nombre().trim());
        auditoria.registrar(actorId, "EDITAR_CATEGORIA", "Se edito la categoria '" + request.nombre() + "'.");
        return CategoriaDto.desde(repositorio.save(categoria));
    }

    @Transactional
    public void inactivar(Long id, Long actorId) {
        Categoria categoria = buscar(id);
        categoria.setActivo(false);
        auditoria.registrar(actorId, "INACTIVAR_CATEGORIA", "Se inactivo la categoria '" + categoria.getNombre() + "'.");
        repositorio.save(categoria);
    }

    private Categoria buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la categoria con id " + id + "."));
    }

    private void validarNombreDisponible(String nombre) {
        repositorio.findByNombre(nombre.trim())
                .ifPresent(c -> {
                    throw new ErrorNegocio("Ya existe una categoria con el nombre '" + nombre + "'.");
                });
    }

}