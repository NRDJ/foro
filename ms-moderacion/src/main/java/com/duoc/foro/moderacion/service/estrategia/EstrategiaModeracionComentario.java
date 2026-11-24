package com.duoc.foro.moderacion.service.estrategia;

import com.duoc.foro.moderacion.client.ForosClient;
import com.duoc.foro.moderacion.model.TipoContenido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstrategiaModeracionComentario implements EstrategiaModeracion {

    private final ForosClient forosClient;

    @Override
    public TipoContenido tipo() {
        return TipoContenido.COMENTARIO;
    }

    @Override
    public void banear(Long contenidoId, String token) {
        forosClient.cambiarEstado("comentarios", "comentario", contenidoId, "BANEADO", token);
    }

    @Override
    public void restaurar(Long contenidoId, String token) {
        forosClient.cambiarEstado("comentarios", "comentario", contenidoId, "VISIBLE", token);
    }
}
