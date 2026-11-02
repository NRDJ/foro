package com.duoc.foro.foros.exception;

import com.duoc.foro.foros.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/** Traduce toda excepcion a una respuesta JSON con el mismo formato. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Acceso denegado a {}: {}", req.getRequestURI(), ex.getMessage());
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage(), req.getRequestURI(), List.of());
    }

    /** Respaldo ante operaciones simultaneas que pasan las validaciones previas. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacion de integridad en {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT, "La operacion viola una restriccion de la base de datos",
                req.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno", req.getRequestURI(), List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return new ResponseEntity<>(ErrorResponse.de(status.value(), "Bad Request", "Los datos enviados no son validos",
                ruta(request), detalles), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        List<String> detalles = ex.getAllErrors().stream().map(e -> e.getDefaultMessage()).sorted().toList();
        return new ResponseEntity<>(ErrorResponse.de(status.value(), "Bad Request", "Los parametros no son validos",
                ruta(request), detalles), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(ErrorResponse.de(status.value(), "Bad Request",
                "El cuerpo de la solicitud no es un JSON valido o tiene valores no permitidos",
                ruta(request), List.of()), headers, status);
    }

    /** Cubre el resto de los errores de Spring MVC: 404 de ruta, 405, 415, id con formato incorrecto, etc. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        HttpStatus estado = HttpStatus.valueOf(status.value());
        return new ResponseEntity<>(ErrorResponse.de(status.value(), estado.getReasonPhrase(), ex.getMessage(),
                ruta(request), List.of()), headers, status);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, String ruta, List<String> detalles) {
        return ResponseEntity.status(status).body(ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, ruta, detalles));
    }

    private String ruta(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
