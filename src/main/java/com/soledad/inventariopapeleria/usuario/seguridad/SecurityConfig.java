package com.soledad.inventariopapeleria.usuario.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Seguridad de la aplicacion (RF-43). El frontend es estatico y consume una
 * API JSON con sesiones HTTP, por eso los errores de autenticacion se
 * devuelven como JSON en lugar de redirigir.
 */
@Configuration
public class SecurityConfig {

    // RN-43: las claves se cifran con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityContextRepository securityContextRepository) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
                .sessionManagement(SecurityConfig::sesiones)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/login.html", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/sonidos/**", "/images/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/usuario/**").hasRole("ADMIN")
                        .requestMatchers("/api/configuracion/**").hasRole("ADMIN")
                        .requestMatchers("/api/auditoria/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categoria/**", "/api/proveedor/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categoria/**", "/api/proveedor/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categoria/**", "/api/proveedor/**").hasRole("ADMIN")
                        .requestMatchers("/api/categoria/**", "/api/proveedor/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(SecurityConfig::denegado));
        return http.build();
    }

    private static void sesiones(SessionManagementConfigurer<HttpSecurity> sm) {
        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    private static void denegado(HttpServletRequest req, HttpServletResponse res,
                                 org.springframework.security.access.AccessDeniedException ex) throws IOException {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(ErrorJson.json("No tiene permiso para realizar esta accion.", req));
    }

    /** Cuerpo de error JSON en es-CO (RNF-03), igual a {@code ErrorRespuesta}. */
    public static final class ErrorJson {

        private ErrorJson() {
        }

        static String json(String mensaje, HttpServletRequest req) {
            return "{\"timestamp\":\"" + Instant.now() + "\",\"mensaje\":\"" + mensaje + "\",\"ruta\":\""
                    + req.getRequestURI() + "\"}";
        }

    }

}