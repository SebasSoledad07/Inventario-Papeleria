package com.soledad.inventariopapeleria.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones del negocio en respuestas HTTP con mensajes en
 * espanol de Colombia (RNF-03).
 */
@RestControllerAdvice
public class GlobalHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(RecursoNoEncontrado.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontrado ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ErrorNegocio.class)
    public ResponseEntity<ErrorRespuesta> negocio(ErrorNegocio ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta accion.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {} ", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado. Intente de nuevo.", req);
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus estado, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(estado).body(ErrorRespuesta.de(mensaje, req.getRequestURI()));
    }

}