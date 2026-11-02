package com.duoc.foro.foros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "El contenido es obligatorio")
        @Size(max = 1000, message = "El contenido no puede superar los 1000 caracteres")
        String contenido) {
}
