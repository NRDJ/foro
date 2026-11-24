package com.duoc.foro.moderacion.service.estrategia;

import com.duoc.foro.moderacion.model.TipoContenido;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Patrón Factory: entrega la estrategia que corresponde a cada tipo de contenido.
 * Spring inyecta todas las implementaciones de {@link EstrategiaModeracion} y aquí se indexan por tipo.
 */
@Component
public class EstrategiaModeracionFactory {

    private final Map<TipoContenido, EstrategiaModeracion> estrategias = new EnumMap<>(TipoContenido.class);

    public EstrategiaModeracionFactory(List<EstrategiaModeracion> implementaciones) {
        implementaciones.forEach(e -> estrategias.put(e.tipo(), e));
        for (TipoContenido tipo : TipoContenido.values()) {
            if (!estrategias.containsKey(tipo)) {
                throw new IllegalStateException("Falta la estrategia de moderación para " + tipo);
            }
        }
    }

    public EstrategiaModeracion obtener(TipoContenido tipo) {
        return estrategias.get(tipo);
    }
}
