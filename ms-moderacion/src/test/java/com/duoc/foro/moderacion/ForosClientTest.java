package com.duoc.foro.moderacion;

import com.duoc.foro.moderacion.client.ForosClient;
import com.duoc.foro.moderacion.exception.RecursoNoEncontradoException;
import com.duoc.foro.moderacion.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Verifica la llamada HTTP real que ms-moderacion le hace a ms-foros. */
class ForosClientTest {

    private MockRestServiceServer servidor;
    private ForosClient cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new ForosClient(builder, "http://foros.test");
    }

    @Test
    void enviaPatchConElTokenYElEstado() {
        servidor.expect(requestTo("http://foros.test/api/temas/5/estado"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer abc"))
                .andExpect(content().json("{\"estado\":\"BANEADO\"}"))
                .andRespond(withSuccess());
        cliente.cambiarEstado("temas", "tema", 5L, "BANEADO", "abc");
        servidor.verify();
    }

    @Test
    void traduceLosErroresDeForos() {
        servidor.expect(requestTo("http://foros.test/api/comentarios/9/estado")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> cliente.cambiarEstado("comentarios", "comentario", 9L, "VISIBLE", "abc"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe el comentario con id 9");

        servidor.reset();
        servidor.expect(requestTo("http://foros.test/api/temas/1/estado")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> cliente.cambiarEstado("temas", "tema", 1L, "BANEADO", "abc"))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
