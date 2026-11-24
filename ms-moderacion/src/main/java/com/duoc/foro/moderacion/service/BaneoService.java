package com.duoc.foro.moderacion.service;

import com.duoc.foro.moderacion.dto.BaneoRequest;
import com.duoc.foro.moderacion.dto.BaneoResponse;
import com.duoc.foro.moderacion.dto.MotivoRequest;
import com.duoc.foro.moderacion.model.TipoContenido;
import com.duoc.foro.moderacion.security.UsuarioActual;

import java.util.List;

public interface BaneoService {

    List<BaneoResponse> listar(TipoContenido tipo);

    BaneoResponse obtener(Long id);

    BaneoResponse banear(BaneoRequest request, UsuarioActual admin, String token);

    BaneoResponse actualizarMotivo(Long id, MotivoRequest request, UsuarioActual admin);

    void revertir(Long id, UsuarioActual admin, String token);
}
