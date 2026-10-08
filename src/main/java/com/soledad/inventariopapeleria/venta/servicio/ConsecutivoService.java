package com.soledad.inventariopapeleria.venta.servicio;

import com.soledad.inventariopapeleria.venta.repositorio.ConsecutivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera consecutivos de venta sin saltos (RN-31). Usa UPDATE ... RETURNING
 * dentro de la transaccion.
 */
@Service
public class ConsecutivoService {

    private final ConsecutivoRepository repositorio;

    public ConsecutivoService(ConsecutivoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Integer siguienteVenta() {
        return repositorio.obtenerSiguienteConsecutivoVenta();
    }
}