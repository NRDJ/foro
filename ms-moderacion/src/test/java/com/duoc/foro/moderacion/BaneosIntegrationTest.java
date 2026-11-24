package com.duoc.foro.moderacion;

import com.duoc.foro.moderacion.client.ForosClient;
import com.duoc.foro.moderacion.exception.RecursoNoEncontradoException;
import com.duoc.foro.moderacion.exception.ServicioNoDisponibleException;
import com.duoc.foro.moderacion.repository.BaneoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BaneosIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired BaneoRepository repository;
    @MockitoBean ForosClient forosClient;

    @Value("${app.jwt.secret}") String secreto;

    private String admin;
    private String tokenAdmin;
    private String usuario;

    @BeforeEach
    void datos() {
        repository.deleteAll();
        reset(forosClient);
        tokenAdmin = token(1L, "admin", "ADMIN");
        admin = "Bearer " + tokenAdmin;
        usuario = "Bearer " + token(2L, "camila", "USUARIO");
    }

    private String token(Long id, String username, String rol) {
        var key = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("ms-usuarios").subject(username)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600))
                .claim("uid", id).claim("rol", rol).build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(key))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private String cuerpo(String tipo, long id) {
        return "{\"tipo\":\"" + tipo + "\",\"contenidoId\":" + id + ",\"motivo\":\"Spam reiterado\"}";
    }

    private long banear(String tipo, long id) throws Exception {
        String r = mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(tipo, id)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(r).get("id").asLong();
    }

    @Test
    void soloElAdministradorModera() throws Exception {
        mvc.perform(get("/api/baneos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/baneos").header(HttpHeaders.AUTHORIZATION, usuario))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, usuario)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo("TEMA", 5))).andExpect(status().isForbidden());
        verify(forosClient, never()).cambiarEstado(anyString(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void banearUsaLaEstrategiaDeCadaTipoYReenviaElToken() throws Exception {
        mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("TEMA", 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("TEMA"))
                .andExpect(jsonPath("$.contenidoId").value(5))
                .andExpect(jsonPath("$.moderadorUsername").value("admin"));
        verify(forosClient).cambiarEstado("temas", "tema", 5L, "BANEADO", tokenAdmin);

        banear("COMENTARIO", 9);
        verify(forosClient).cambiarEstado("comentarios", "comentario", 9L, "BANEADO", tokenAdmin);
    }

    @Test
    void noSeBaneaDosVecesElMismoContenido() throws Exception {
        banear("TEMA", 5);
        mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo("TEMA", 5))).andExpect(status().isConflict());
        verify(forosClient).cambiarEstado(eq("temas"), anyString(), eq(5L), eq("BANEADO"), anyString());
    }

    @Test
    void siElContenidoNoExisteOForosNoRespondeNoSeGuardaNada() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el tema con id 77"))
                .when(forosClient).cambiarEstado(any(), any(), eq(77L), any(), any());
        mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("TEMA", 77)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el tema con id 77"));

        doThrow(new ServicioNoDisponibleException("No se pudo contactar a ms-foros"))
                .when(forosClient).cambiarEstado(any(), any(), eq(78L), any(), any());
        mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("TEMA", 78)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
        assertThat(repository.count()).isZero();
    }

    @Test
    void crudCompletoDelBaneo() throws Exception {
        long id = banear("TEMA", 5);
        banear("COMENTARIO", 9);

        mvc.perform(get("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/baneos").param("tipo", "COMENTARIO").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contenidoId").value(9));
        mvc.perform(get("/api/baneos/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.motivo").value("Spam reiterado"));
        mvc.perform(put("/api/baneos/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Publicidad engañosa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("Publicidad engañosa"));

        mvc.perform(delete("/api/baneos/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        verify(forosClient).cambiarEstado("temas", "tema", 5L, "VISIBLE", tokenAdmin);
        mvc.perform(get("/api/baneos/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void revertirFuncionaAunqueElContenidoYaNoExista() throws Exception {
        long id = banear("TEMA", 5);
        doThrow(new RecursoNoEncontradoException("No existe el tema con id 5"))
                .when(forosClient).cambiarEstado("temas", "tema", 5L, "VISIBLE", tokenAdmin);
        mvc.perform(delete("/api/baneos/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        assertThat(repository.count()).isZero();
    }

    @Test
    void validaLosDatos() throws Exception {
        mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"contenidoId\":-1,\"motivo\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(3));
        mvc.perform(post("/api/baneos").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"USUARIO\",\"contenidoId\":1,\"motivo\":\"Spam reiterado\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/baneos").param("tipo", "OTRO").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
    }
}
