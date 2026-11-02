package com.duoc.foro.foros;

import com.duoc.foro.foros.model.Categoria;
import com.duoc.foro.foros.repository.CategoriaRepository;
import com.duoc.foro.foros.repository.ComentarioRepository;
import com.duoc.foro.foros.repository.TemaRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ForosIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired CategoriaRepository categorias;
    @Autowired TemaRepository temas;
    @Autowired ComentarioRepository comentarios;

    @Value("${app.jwt.secret}") String secreto;
    @Value("${app.jwt.issuer}") String issuer;

    private Long categoriaId;
    private String admin;
    private String camila;
    private String diego;

    @BeforeEach
    void datos() {
        comentarios.deleteAll();
        temas.deleteAll();
        categorias.deleteAll();
        Categoria c = new Categoria();
        c.setNombre("Tecnología");
        c.setDescripcion("Programación");
        categoriaId = categorias.save(c).getId();
        admin = token(1L, "admin", "ADMIN", secreto, issuer);
        camila = token(2L, "camila", "USUARIO", secreto, issuer);
        diego = token(3L, "diego", "USUARIO", secreto, issuer);
    }

    private String token(Long id, String username, String rol, String clave, String emisor) {
        var key = new SecretKeySpec(clave.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(emisor).subject(username)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600))
                .claim("uid", id).claim("rol", rol).build();
        return "Bearer " + new NimbusJwtEncoder(new ImmutableSecret<>(key))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private String json(String titulo) {
        return "{\"titulo\":\"" + titulo + "\",\"contenido\":\"Contenido de " + titulo + "\",\"categoriaId\":" + categoriaId + "}";
    }

    private long publicar(String titulo, String token) throws Exception {
        String cuerpo = mvc.perform(post("/api/temas").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(titulo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(cuerpo).get("id").asLong();
    }

    private long comentar(long temaId, String token) throws Exception {
        String cuerpo = mvc.perform(post("/api/temas/" + temaId + "/comentarios").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"contenido\":\"Buen tema\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(cuerpo).get("id").asLong();
    }

    @Test
    void lecturaEsPublicaYPublicarExigeSesion() throws Exception {
        mvc.perform(get("/api/temas")).andExpect(status().isOk());
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mvc.perform(post("/api/temas").contentType(MediaType.APPLICATION_JSON).content(json("X")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rechazaTokenFirmadoConOtraClaveOEmisor() throws Exception {
        String otraClave = token(2L, "camila", "ADMIN", "otra-clave-distinta-con-mas-de-32-caracteres", issuer);
        String otroEmisor = token(2L, "camila", "ADMIN", secreto, "otro-servicio");
        mvc.perform(post("/api/temas").header(HttpHeaders.AUTHORIZATION, otraClave)
                .contentType(MediaType.APPLICATION_JSON).content(json("X"))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/temas").header(HttpHeaders.AUTHORIZATION, otroEmisor)
                .contentType(MediaType.APPLICATION_JSON).content(json("X"))).andExpect(status().isUnauthorized());
    }

    @Test
    void elAutorSaleDelTokenYNoDelCuerpo() throws Exception {
        mvc.perform(post("/api/temas").header(HttpHeaders.AUTHORIZATION, camila).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"T\",\"contenido\":\"C\",\"categoriaId\":" + categoriaId + ",\"autorId\":99,\"autorUsername\":\"falso\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorId").value(2))
                .andExpect(jsonPath("$.autorUsername").value("camila"))
                .andExpect(jsonPath("$.categoriaNombre").value("Tecnología"))
                .andExpect(jsonPath("$.totalComentarios").value(0));
    }

    @Test
    void busquedaCombinaFiltrosPaginaYCuentaComentarios() throws Exception {
        long oracle = publicar("Aprendiendo ORACLE", camila);
        publicar("Spring Boot", camila);
        publicar("Oracle avanzado", diego);
        comentar(oracle, diego);
        comentar(oracle, camila);

        mvc.perform(get("/api/temas").param("q", "oracle"))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.contenido[0].titulo").value("Oracle avanzado"))
                .andExpect(jsonPath("$.contenido[1].totalComentarios").value(2));
        mvc.perform(get("/api/temas").param("q", "oracle").param("autorId", "2"))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/api/temas").param("categoriaId", categoriaId.toString()).param("size", "2").param("page", "1"))
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        mvc.perform(get("/api/temas").param("categoriaId", "999999"))
                .andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(get("/api/temas").param("q", "%"))
                .andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(get("/api/temas").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value("size debe ser al menos 1"));
    }

    @Test
    void detalleIncluyeComentariosEnOrden() throws Exception {
        long tema = publicar("Tema", camila);
        comentar(tema, diego);
        comentar(tema, camila);
        mvc.perform(get("/api/temas/" + tema))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tema.totalComentarios").value(2))
                .andExpect(jsonPath("$.comentarios[0].autorUsername").value("diego"))
                .andExpect(jsonPath("$.comentarios[1].autorUsername").value("camila"));
        mvc.perform(get("/api/temas/" + tema + "/comentarios"))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/temas/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/temas/999999/comentarios")).andExpect(status().isNotFound());
    }

    @Test
    void soloElAutorOUnAdminModificanYEliminanTemas() throws Exception {
        long tema = publicar("Original", camila);
        mvc.perform(put("/api/temas/" + tema).header(HttpHeaders.AUTHORIZATION, diego)
                .contentType(MediaType.APPLICATION_JSON).content(json("Ajeno"))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/temas/" + tema).header(HttpHeaders.AUTHORIZATION, diego))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/temas/" + tema).header(HttpHeaders.AUTHORIZATION, camila)
                        .contentType(MediaType.APPLICATION_JSON).content(json("Editado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Editado"));
        mvc.perform(put("/api/temas/" + tema).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json("Moderado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autorUsername").value("camila"));
        mvc.perform(delete("/api/temas/" + tema).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarTemaEliminaSusComentarios() throws Exception {
        long tema = publicar("Con comentarios", camila);
        long comentario = comentar(tema, diego);
        mvc.perform(delete("/api/temas/" + tema).header(HttpHeaders.AUTHORIZATION, camila))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/comentarios/" + comentario)).andExpect(status().isNotFound());
    }

    @Test
    void soloElAutorOUnAdminModificanYEliminanComentarios() throws Exception {
        long tema = publicar("Tema", camila);
        long comentario = comentar(tema, diego);
        String cuerpo = "{\"contenido\":\"Editado\"}";
        mvc.perform(put("/api/comentarios/" + comentario).header(HttpHeaders.AUTHORIZATION, camila)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isForbidden());
        mvc.perform(put("/api/comentarios/" + comentario).header(HttpHeaders.AUTHORIZATION, diego)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").value("Editado"));
        mvc.perform(delete("/api/comentarios/" + comentario).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void categoriasSoloLasGestionaUnAdmin() throws Exception {
        String cuerpo = "{\"nombre\":\"Cocina\",\"descripcion\":\"Recetas\"}";
        mvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, camila)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        String creada = mvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(creada).get("id").asLong();
        mvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"COCINA\",\"descripcion\":\"Otra\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/categorias/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Cocina\",\"descripcion\":\"Nueva\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Nueva"));
        mvc.perform(delete("/api/categorias/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void categoriaConTemasNoSePuedeEliminarYMuestraTotal() throws Exception {
        publicar("Tema", camila);
        mvc.perform(get("/api/categorias/" + categoriaId))
                .andExpect(jsonPath("$.totalTemas").value(1));
        mvc.perform(delete("/api/categorias/" + categoriaId).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isConflict());
    }

    @Test
    void validacionYFormatoRespondenJson() throws Exception {
        mvc.perform(post("/api/temas").header(HttpHeaders.AUTHORIZATION, camila)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(3));
        mvc.perform(post("/api/temas").header(HttpHeaders.AUTHORIZATION, camila)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"T\",\"contenido\":\"C\",\"categoriaId\":999999}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/temas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
