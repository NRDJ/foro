package com.duoc.foro.foros.service;

import com.duoc.foro.foros.dto.ComentarioRequest;
import com.duoc.foro.foros.dto.ComentarioResponse;
import com.duoc.foro.foros.model.EstadoContenido;
import com.duoc.foro.foros.security.UsuarioActual;

import java.util.List;

public interface ComentarioService {

    /** {@code actual} es null cuando quien consulta no inició sesión. */
    List<ComentarioResponse> listarPorTema(Long temaId, UsuarioActual actual);

    ComentarioResponse obtener(Long id, UsuarioActual actual);

    ComentarioResponse comentar(Long temaId, ComentarioRequest request, UsuarioActual autor);

    ComentarioResponse actualizar(Long id, ComentarioRequest request, UsuarioActual actual);

    void eliminar(Long id, UsuarioActual actual);

    ComentarioResponse cambiarEstado(Long id, EstadoContenido estado, UsuarioActual admin);
}
