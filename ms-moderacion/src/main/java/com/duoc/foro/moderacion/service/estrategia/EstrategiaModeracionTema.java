package com.duoc.foro.moderacion.service.estrategia;

import com.duoc.foro.moderacion.client.ForosClient;
import com.duoc.foro.moderacion.model.TipoContenido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstrategiaModeracionTema implements EstrategiaModeracion {

    private final ForosClient forosClient;

    @Override
    public TipoContenido tipo() {
        return TipoContenido.TEMA;
    }

    @Override
    public void banear(Long contenidoId, String token) {
        forosClient.cambiarEstado("temas", "tema", contenidoId, "BANEADO", token);
    }

    @Override
    public void restaurar(Long contenidoId, String token) {
        forosClient.cambiarEstado("temas", "tema", contenidoId, "VISIBLE", token);
    }
}
