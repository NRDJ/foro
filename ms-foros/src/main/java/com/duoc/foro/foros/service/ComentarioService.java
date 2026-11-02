package com.duoc.foro.foros.service;

import com.duoc.foro.foros.dto.ComentarioRequest;
import com.duoc.foro.foros.dto.ComentarioResponse;
import com.duoc.foro.foros.security.UsuarioActual;

import java.util.List;

public interface ComentarioService {

    List<ComentarioResponse> listarPorTema(Long temaId);

    ComentarioResponse obtener(Long id);

    ComentarioResponse comentar(Long temaId, ComentarioRequest request, UsuarioActual autor);

    ComentarioResponse actualizar(Long id, ComentarioRequest request, UsuarioActual actual);

    void eliminar(Long id, UsuarioActual actual);
}
