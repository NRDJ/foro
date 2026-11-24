package com.duoc.foro.moderacion.controller;

import com.duoc.foro.moderacion.dto.BaneoRequest;
import com.duoc.foro.moderacion.dto.BaneoResponse;
import com.duoc.foro.moderacion.dto.MotivoRequest;
import com.duoc.foro.moderacion.model.TipoContenido;
import com.duoc.foro.moderacion.security.UsuarioActual;
import com.duoc.foro.moderacion.service.BaneoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Todos los endpoints exigen rol ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/baneos")
@CrossOrigin(origins = "${app.cors.origen}")
@RequiredArgsConstructor
public class BaneoController {

    private final BaneoService service;

    @GetMapping
    public List<BaneoResponse> listar(@RequestParam(required = false) TipoContenido tipo) {
        return service.listar(tipo);
    }

    @GetMapping("/{id}")
    public BaneoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaneoResponse banear(@Valid @RequestBody BaneoRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.banear(request, UsuarioActual.desde(jwt), jwt.getTokenValue());
    }

    @PutMapping("/{id}")
    public BaneoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MotivoRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarMotivo(id, request, UsuarioActual.desde(jwt));
    }

    /** Revierte el baneo: el contenido vuelve a ser visible en ms-foros. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revertir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.revertir(id, UsuarioActual.desde(jwt), jwt.getTokenValue());
    }
}
