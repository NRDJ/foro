package com.duoc.foro.foros.service;

import com.duoc.foro.foros.dto.PaginaResponse;
import com.duoc.foro.foros.dto.TemaDetalleResponse;
import com.duoc.foro.foros.dto.TemaRequest;
import com.duoc.foro.foros.dto.TemaResponse;
import com.duoc.foro.foros.security.UsuarioActual;

public interface TemaService {

    PaginaResponse<TemaResponse> buscar(String texto, Long categoriaId, Long autorId, int pagina, int tamano);

    TemaDetalleResponse obtenerDetalle(Long id);

    TemaResponse publicar(TemaRequest request, UsuarioActual autor);

    TemaResponse actualizar(Long id, TemaRequest request, UsuarioActual actual);

    void eliminar(Long id, UsuarioActual actual);
}
