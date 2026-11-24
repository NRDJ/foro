package com.duoc.foro.moderacion.client;

import com.duoc.foro.moderacion.exception.RecursoNoEncontradoException;
import com.duoc.foro.moderacion.exception.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Cliente HTTP hacia ms-foros. Reenvía el token del administrador, así ms-foros
 * vuelve a comprobar que quien banea tiene rol ADMIN.
 */
@Slf4j
@Component
public class ForosClient {

    private final RestClient restClient;

    public ForosClient(RestClient.Builder builder, @Value("${app.foros.url}") String urlForos) {
        // Los tiempos máximos de conexión y respuesta están en application.properties (spring.http.client.*).
        this.restClient = builder.baseUrl(urlForos).build();
    }

    /**
     * @param recurso "temas" o "comentarios"
     * @param estado  "BANEADO" o "VISIBLE"
     */
    public void cambiarEstado(String recurso, String nombre, Long id, String estado, String token) {
        try {
            restClient.patch()
                    .uri("/api/{recurso}/{id}/estado", recurso, id)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", estado))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("No existe el " + nombre + " con id " + id);
        } catch (RestClientResponseException e) {
            log.error("ms-foros respondió {} al cambiar el estado de {} {}", e.getStatusCode(), nombre, id);
            throw new ServicioNoDisponibleException("ms-foros rechazó la operación (" + e.getStatusCode().value() + ")");
        } catch (ResourceAccessException e) {
            log.error("No se pudo contactar a ms-foros: {}", e.getMessage());
            throw new ServicioNoDisponibleException("No se pudo contactar a ms-foros");
        }
    }
}
