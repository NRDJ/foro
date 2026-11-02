package com.duoc.foro.foros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TemaRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 150, message = "El titulo no puede superar los 150 caracteres")
        String titulo,

        @NotBlank(message = "El contenido es obligatorio")
        @Size(max = 1000, message = "El contenido no puede superar los 1000 caracteres")
        String contenido,

        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId) {
}
