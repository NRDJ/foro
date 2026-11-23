package com.duoc.foro.foros.dto;

import com.duoc.foro.foros.model.EstadoContenido;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(
        @NotNull(message = "El estado es obligatorio (VISIBLE o BANEADO)")
        EstadoContenido estado) {
}
