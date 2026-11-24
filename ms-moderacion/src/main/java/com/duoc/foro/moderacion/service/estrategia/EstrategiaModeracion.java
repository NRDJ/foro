package com.duoc.foro.moderacion.service.estrategia;

import com.duoc.foro.moderacion.model.TipoContenido;

/**
 * Patrón Strategy: cada tipo de contenido sabe cómo ocultarse y restaurarse en ms-foros.
 * Agregar un tipo nuevo solo requiere otra implementación; el servicio no cambia.
 */
public interface EstrategiaModeracion {

    TipoContenido tipo();

    void banear(Long contenidoId, String token);

    void restaurar(Long contenidoId, String token);
}
