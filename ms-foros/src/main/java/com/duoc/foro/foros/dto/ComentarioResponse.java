package com.duoc.foro.foros.dto;

import com.duoc.foro.foros.model.Comentario;

import java.time.LocalDateTime;

public record ComentarioResponse(
        Long id,
        String contenido,
        Long temaId,
        Long autorId,
        String autorUsername,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static ComentarioResponse desde(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getContenido(), c.getTema().getId(), c.getAutorId(),
                c.getAutorUsername(), c.getFechaCreacion(), c.getFechaActualizacion());
    }
}
