package com.soledad.inventariopapeleria.configuracion.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.configuracion.api.ConfiguracionDto;
import com.soledad.inventariopapeleria.configuracion.dominio.Configuracion;
import com.soledad.inventariopapeleria.configuracion.repositorio.ConfiguracionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos del negocio y parametros (RF-45, RF-46). Solo ADMIN (ver
 * SecurityConfig). Los cambios quedan en auditoria (RN-45).
 */
@Service
public class ConfiguracionService {

    private final ConfiguracionRepository repositorio;
    private final AuditoriaService auditoria;

    public ConfiguracionService(ConfiguracionRepository repositorio, AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<ConfiguracionDto> listar() {
        return repositorio.findAllByOrderByClaveAsc().stream()
                .map(ConfiguracionDto::desde)
                .toList();
    }

    @Transactional
    public List<ConfiguracionDto> actualizar(Map<String, String> valores, Long actorId) {
        valores.forEach((clave, valor) -> {
            Configuracion configuracion = repositorio.findById(clave).orElseGet(() -> {
                Configuracion nueva = new Configuracion();
                nueva.setClave(clave);
                return nueva;
            });
            configuracion.setValor(valor);
            configuracion.setActualizadoEn(Instant.now());
            repositorio.save(configuracion);
        });
        auditoria.registrar(actorId, "EDITAR_CONFIGURACION",
                "Se actualizaron " + valores.size() + " parametros de configuracion.");
        return listar();
    }

}