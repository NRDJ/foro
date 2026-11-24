package com.duoc.foro.moderacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Al modificar un baneo solo cambia el motivo; el contenido baneado no se puede cambiar. */
public record MotivoRequest(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
        String motivo) {
}
