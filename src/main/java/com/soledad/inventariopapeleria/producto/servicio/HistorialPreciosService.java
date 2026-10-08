package com.soledad.inventariopapeleria.producto.servicio;

import com.soledad.inventariopapeleria.producto.dominio.HistorialPrecios;
import com.soledad.inventariopapeleria.producto.repositorio.HistorialPreciosRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialPreciosService {

    private final HistorialPreciosRepository repositorio;

    public HistorialPreciosService(HistorialPreciosRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<HistorialPrecios> listar() {
        return repositorio.findAll();
    }
}