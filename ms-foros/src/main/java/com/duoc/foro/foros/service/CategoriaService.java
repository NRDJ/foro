package com.duoc.foro.foros.service;

import com.duoc.foro.foros.dto.CategoriaRequest;
import com.duoc.foro.foros.dto.CategoriaResponse;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponse> listar();

    CategoriaResponse obtener(Long id);

    CategoriaResponse crear(CategoriaRequest request);

    CategoriaResponse actualizar(Long id, CategoriaRequest request);

    void eliminar(Long id);
}
