package com.duoc.foro.foros.controller;

import com.duoc.foro.foros.dto.ComentarioRequest;
import com.duoc.foro.foros.dto.ComentarioResponse;
import com.duoc.foro.foros.dto.EstadoRequest;
import com.duoc.foro.foros.security.UsuarioActual;
import com.duoc.foro.foros.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comentarios")
@CrossOrigin(origins = "${app.cors.origen}")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService service;

    @GetMapping("/{id}")
    public ComentarioResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, UsuarioActual.desdeOpcional(jwt));
    }

    /** Lo usa ms-moderacion para banear (BANEADO) o restaurar (VISIBLE) un comentario. Solo ADMIN. */
    @PatchMapping("/{id}/estado")
    public ComentarioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return service.cambiarEstado(id, request.estado(), UsuarioActual.desde(jwt));
    }

    @PutMapping("/{id}")
    public ComentarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ComentarioRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return service.actualizar(id, request, UsuarioActual.desde(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminar(id, UsuarioActual.desde(jwt));
    }
}
