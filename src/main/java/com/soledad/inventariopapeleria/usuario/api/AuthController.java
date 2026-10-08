package com.soledad.inventariopapeleria.usuario.api;

import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.usuario.seguridad.UsuarioDatos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inicio y cierre de sesion (RF-43).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<SesionDto> login(@Valid @RequestBody LoginRequest request,
                                           HttpServletRequest req, HttpServletResponse res) {
        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.clave()));
        } catch (RuntimeException e) {
            throw new ErrorNegocio("Usuario o contrasena incorrectos.");
        }

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, req, res);

        UsuarioDatos datos = (UsuarioDatos) autenticacion.getPrincipal();
        return ResponseEntity.ok(new SesionDto(datos.getNombre(), datos.getRol().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req) {
        SecurityContextHolder.clearContext();
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sesion")
    public ResponseEntity<SesionDto> sesion() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioDatos datos) {
            return ResponseEntity.ok(new SesionDto(datos.getNombre(), datos.getRol().name()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}