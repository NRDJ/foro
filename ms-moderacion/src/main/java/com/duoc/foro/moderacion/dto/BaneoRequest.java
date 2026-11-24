package com.duoc.foro.moderacion.dto;

import com.duoc.foro.moderacion.model.TipoContenido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BaneoRequest(
        @NotNull(message = "El tipo es obligatorio (TEMA o COMENTARIO)")
        TipoContenido tipo,

        @NotNull(message = "El id del contenido es obligatorio")
        @Positive(message = "El id del contenido debe ser positivo")
        Long contenidoId,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
        String motivo) {
}
