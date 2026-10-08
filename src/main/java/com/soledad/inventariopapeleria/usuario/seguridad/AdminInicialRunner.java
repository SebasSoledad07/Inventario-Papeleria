package com.soledad.inventariopapeleria.usuario.seguridad;

import com.soledad.inventariopapeleria.usuario.api.CrearUsuarioRequest;
import com.soledad.inventariopapeleria.usuario.dominio.Rol;
import com.soledad.inventariopapeleria.usuario.repositorio.UsuarioRepository;
import com.soledad.inventariopapeleria.usuario.servicio.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * // PENDIENTE-DECISION-4
 * Crea el administrador inicial al arrancar si la tabla de usuarios esta
 * vacia. Usuario y clave salen de las variables de entorno
 * ADMIN_INICIAL_USUARIO y ADMIN_INICIAL_CLAVE (nunca del codigo). Si la
 * clave esta vacia no crea nada y deja una advertencia en el log.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialRunner.class);

    private final UsuarioRepository usuarioRepositorio;
    private final UsuarioService usuarioService;
    private final Environment environment;

    public AdminInicialRunner(UsuarioRepository usuarioRepositorio, UsuarioService usuarioService,
                              Environment environment) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.usuarioService = usuarioService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepositorio.count() > 0) {
            return;
        }

        String usuario = environment.getProperty("ADMIN_INICIAL_USUARIO", "");
        String clave = environment.getProperty("ADMIN_INICIAL_CLAVE", "");

        if (usuario.isBlank() || clave.isBlank()) {
            log.warn("La tabla de usuarios esta vacia pero faltan ADMIN_INICIAL_USUARIO / "
                    + "ADMIN_INICIAL_CLAVE. No se creo ningun administrador.");
            return;
        }

        usuarioService.crear(new CrearUsuarioRequest(usuario.trim(), "Administrador", clave, Rol.ADMIN.name()), null);
        log.info("Se creo el administrador inicial '{}'. Cambie su clave en el primer ingreso.", usuario.trim());
    }

}