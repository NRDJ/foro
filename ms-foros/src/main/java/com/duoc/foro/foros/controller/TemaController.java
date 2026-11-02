package com.duoc.foro.foros.controller;

import com.duoc.foro.foros.dto.ComentarioRequest;
import com.duoc.foro.foros.dto.ComentarioResponse;
import com.duoc.foro.foros.dto.PaginaResponse;
import com.duoc.foro.foros.dto.TemaDetalleResponse;
import com.duoc.foro.foros.dto.TemaRequest;
import com.duoc.foro.foros.dto.TemaResponse;
import com.duoc.foro.foros.security.UsuarioActual;
import com.duoc.foro.foros.service.ComentarioService;
import com.duoc.foro.foros.service.TemaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

@RestController
@RequestMapping("/api/temas")
@CrossOrigin(origins = "${app.cors.origen}")
@RequiredArgsConstructor
public class TemaController {

    private final TemaService temaService;
    private final ComentarioService comentarioService;

    @GetMapping
    public PaginaResponse<TemaResponse> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long autorId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page no puede ser negativo") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size debe ser al menos 1")
            @Max(value = 100, message = "size no puede superar 100") int size) {
        return temaService.buscar(q, categoriaId, autorId, page, size);
    }

    @GetMapping("/{id}")
    public TemaDetalleResponse obtener(@PathVariable Long id) {
        return temaService.obtenerDetalle(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemaResponse publicar(@Valid @RequestBody TemaRequest request, @AuthenticationPrincipal Jwt jwt) {
        return temaService.publicar(request, UsuarioActual.desde(jwt));
    }

    @PutMapping("/{id}")
    public TemaResponse actualizar(@PathVariable Long id, @Valid @RequestBody TemaRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return temaService.actualizar(id, request, UsuarioActual.desde(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        temaService.eliminar(id, UsuarioActual.desde(jwt));
    }

    @GetMapping("/{id}/comentarios")
    public List<ComentarioResponse> listarComentarios(@PathVariable Long id) {
        return comentarioService.listarPorTema(id);
    }

    @PostMapping("/{id}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse comentar(@PathVariable Long id, @Valid @RequestBody ComentarioRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return comentarioService.comentar(id, request, UsuarioActual.desde(jwt));
    }
}
