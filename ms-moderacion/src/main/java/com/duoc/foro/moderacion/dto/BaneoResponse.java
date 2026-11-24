package com.duoc.foro.moderacion.dto;

import com.duoc.foro.moderacion.model.Baneo;
import com.duoc.foro.moderacion.model.TipoContenido;
import lombok.Builder;

import java.time.LocalDateTime;

/** Patrón Builder (vía Lombok): la respuesta se arma campo a campo con nombre, sin depender del orden. */
@Builder
public record BaneoResponse(
        Long id,
        TipoContenido tipo,
        Long contenidoId,
        String motivo,
        Long moderadorId,
        String moderadorUsername,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static BaneoResponse desde(Baneo b) {
        return BaneoResponse.builder()
                .id(b.getId())
                .tipo(b.getTipo())
                .contenidoId(b.getContenidoId())
                .motivo(b.getMotivo())
                .moderadorId(b.getModeradorId())
                .moderadorUsername(b.getModeradorUsername())
                .fechaCreacion(b.getFechaCreacion())
                .fechaActualizacion(b.getFechaActualizacion())
                .build();
    }
}
