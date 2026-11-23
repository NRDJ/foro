package com.duoc.foro.foros.dto;

import com.duoc.foro.foros.model.EstadoContenido;
import com.duoc.foro.foros.model.Tema;

import java.time.LocalDateTime;

public record TemaResponse(
        Long id,
        String titulo,
        String contenido,
        Long categoriaId,
        String categoriaNombre,
        Long autorId,
        String autorUsername,
        EstadoContenido estado,
        long totalComentarios,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static TemaResponse desde(Tema t, long totalComentarios) {
        return new TemaResponse(t.getId(), t.getTitulo(), t.getContenido(), t.getCategoria().getId(),
                t.getCategoria().getNombre(), t.getAutorId(), t.getAutorUsername(), t.getEstado(), totalComentarios,
                t.getFechaCreacion(), t.getFechaActualizacion());
    }
}
