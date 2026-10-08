package com.soledad.inventariopapeleria.auditoria.servicio;

import com.soledad.inventariopapeleria.auditoria.dominio.Auditoria;
import com.soledad.inventariopapeleria.auditoria.repositorio.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de acciones sensibles (RN-45) en la tabla auditoria.
 */
@Service
public class AuditoriaService {

    private final AuditoriaRepository repositorio;

    public AuditoriaService(AuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void registrar(Long usuarioId, String accion, String detalle) {
        // Sin actor (p.ej. arranque del sistema) no se registra: la tabla
        // auditoria exige usuario_id NOT NULL.
        if (usuarioId == null) {
            return;
        }
        Auditoria registro = new Auditoria();
        registro.setUsuarioId(usuarioId);
        registro.setAccion(accion);
        registro.setDetalle(detalle);
        repositorio.save(registro);
    }

}