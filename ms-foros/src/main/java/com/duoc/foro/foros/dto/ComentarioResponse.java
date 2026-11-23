package com.duoc.foro.foros.dto;

import com.duoc.foro.foros.model.Comentario;
import com.duoc.foro.foros.model.EstadoContenido;

import java.time.LocalDateTime;

public record ComentarioResponse(
        Long id,
        String contenido,
        Long temaId,
        Long autorId,
        String autorUsername,
        EstadoContenido estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static ComentarioResponse desde(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getContenido(), c.getTema().getId(), c.getAutorId(),
                c.getAutorUsername(), c.getEstado(), c.getFechaCreacion(), c.getFechaActualizacion());
    }
}
