package com.soledad.inventariopapeleria.unitarias.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.soledad.inventariopapeleria.auditoria.repositorio.AuditoriaRepository;
import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.usuario.api.CrearUsuarioRequest;
import com.soledad.inventariopapeleria.usuario.dominio.Rol;
import com.soledad.inventariopapeleria.usuario.dominio.Usuario;
import com.soledad.inventariopapeleria.usuario.repositorio.UsuarioRepository;
import com.soledad.inventariopapeleria.usuario.servicio.UsuarioService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repositorio;

    @Mock
    private AuditoriaRepository auditoriaRepositorio;

    private UsuarioService servicio;
    private String createdClave;

    @BeforeEach
    void setUp() {
        var codificador = new BCryptPasswordEncoder();
        servicio = new UsuarioService(repositorio, codificador, new AuditoriaService(auditoriaRepositorio));
    }

    @Test
    void creaUsuarioYcifraLaClaveConBCrypt() {
        when(repositorio.findByUsuario("ana")).thenReturn(Optional.empty());
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            this.createdClave = u.getClave();
            return u;
        });

        var creado = servicio.crear(
                new CrearUsuarioRequest("ana", "Ana Lopez", "clave-secreta-123", Rol.CAJERO.name()), 1L);

        assertThat(creado.rol()).isEqualTo(Rol.CAJERO);
        assertThat(createdClave).isNotEqualTo("clave-secreta-123");
        var codificador = new BCryptPasswordEncoder();
        assertThat(codificador.matches("clave-secreta-123", createdClave)).isTrue();
    }

    @Test
    void rechazaUsuarioRepetido() {
        when(repositorio.findByUsuario("ana")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> servicio.crear(
                new CrearUsuarioRequest("ana", "Ana Lopez", "clave-secreta-123", Rol.ADMIN.name()), 1L))
                .isInstanceOf(ErrorNegocio.class)
                .hasMessageContaining("Ya existe un usuario");

        verify(repositorio, never()).save(any());
    }
}
