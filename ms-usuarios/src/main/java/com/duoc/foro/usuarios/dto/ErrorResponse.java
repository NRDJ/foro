package com.duoc.foro.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles) {

    public static ErrorResponse de(int status, String error, String mensaje, String ruta, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, ruta, detalles);
    }
}
