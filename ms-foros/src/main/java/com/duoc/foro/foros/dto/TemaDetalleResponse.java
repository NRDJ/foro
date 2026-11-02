package com.duoc.foro.foros.dto;

import java.util.List;

public record TemaDetalleResponse(TemaResponse tema, List<ComentarioResponse> comentarios) {
}
