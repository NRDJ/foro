package com.duoc.foro.moderacion.event;

import com.duoc.foro.moderacion.model.TipoContenido;

/** Se publica cada vez que un administrador banea o restaura contenido. */
public record ContenidoModeradoEvent(TipoContenido tipo, Long contenidoId, Accion accion, String moderador, String motivo) {

    public enum Accion {
        BANEADO,
        RESTAURADO
    }
}
