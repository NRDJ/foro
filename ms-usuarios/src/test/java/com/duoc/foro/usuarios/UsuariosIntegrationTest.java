package com.duoc.foro.usuarios;

import com.duoc.foro.usuarios.model.Rol;
import com.duoc.foro.usuarios.model.Usuario;
import com.duoc.foro.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuariosIntegrationTest {

    private static final String CLAVE = "Clave2026!";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository repository;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;

    private Usuario admin;
    private Usuario comun;

    @BeforeEach
    void datos() {
        repository.deleteAll();
        admin = guardar("admin", Rol.ADMIN);
        comun = guardar("comun", Rol.USUARIO);
    }

    private Usuario guardar(String username, Rol rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmail(username + "@foro.cl");
        u.setNombre(username);
        u.setPasswordHash(encoder.encode(CLAVE));
        u.setRol(rol);
        return repository.save(u);
    }

    private String token(String username) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(cuerpo).get("token").asText();
    }

    @Test
    void registroPublicoSiempreCreaRolUsuarioYNoExponeLaClave() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"Nuevo.User","email":"Nuevo@Foro.cl","password":"Clave2026!","nombre":"Nuevo","rol":"ADMIN"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nuevo.user"))
                .andExpect(jsonPath("$.email").value("nuevo@foro.cl"))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registroDuplicadoDevuelve409YDatosInvalidos400() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"ADMIN","email":"otro@foro.cl","password":"Clave2026!","nombre":"X"}"""))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"a","email":"malo","password":"corta","nombre":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(4));
    }

    @Test
    void claveQueSuperaLos72BytesDevuelve400() throws Exception {
        String clave = "ñ".repeat(40);
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"username\":\"largo\",\"email\":\"largo@foro.cl\",\"password\":\"" + clave + "\",\"nombre\":\"L\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La contrasena es demasiado larga"));
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contrasena incorrectos"));
    }

    @Test
    void listarExigeRolAdmin() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, token("comun")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, token("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void busquedaFiltraPorTextoYRolYTrataComodinesComoLiterales() throws Exception {
        String tokenAdmin = token("admin");
        mvc.perform(get("/api/usuarios").param("q", "COM").header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("comun"));
        mvc.perform(get("/api/usuarios").param("rol", "ADMIN").header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("admin"));
        mvc.perform(get("/api/usuarios").param("q", "%").header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/usuarios").param("rol", "NOEXISTE").header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usuarioComunSoloAccedeASuCuentaYNoCambiaSuRol() throws Exception {
        String t = token("comun");
        mvc.perform(get("/api/usuarios/" + comun.getId()).header(HttpHeaders.AUTHORIZATION, t))
                .andExpect(status().isOk());
        mvc.perform(get("/api/usuarios/" + admin.getId()).header(HttpHeaders.AUTHORIZATION, t))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/" + comun.getId()).header(HttpHeaders.AUTHORIZATION, t)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"comun@foro.cl\",\"nombre\":\"Comun\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/" + comun.getId()).header(HttpHeaders.AUTHORIZATION, t)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"comun2@foro.cl\",\"nombre\":\"Comun Dos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Comun Dos"))
                .andExpect(jsonPath("$.rol").value("USUARIO"));
    }

    @Test
    void cambioDeClavePermiteEntrarConLaNueva() throws Exception {
        mvc.perform(put("/api/usuarios/" + comun.getId()).header(HttpHeaders.AUTHORIZATION, token("comun"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"comun@foro.cl\",\"nombre\":\"Comun\",\"password\":\"NuevaClave2026\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"comun\",\"password\":\"NuevaClave2026\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void noSePuedeQuitarAlUnicoAdministrador() throws Exception {
        String t = token("admin");
        mvc.perform(delete("/api/usuarios/" + admin.getId()).header(HttpHeaders.AUTHORIZATION, t))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/usuarios/" + admin.getId()).header(HttpHeaders.AUTHORIZATION, t)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@foro.cl\",\"nombre\":\"Admin\",\"rol\":\"USUARIO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCreaYEliminaUsuarios() throws Exception {
        String t = token("admin");
        String cuerpo = mvc.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, t)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"username":"admin2","email":"admin2@foro.cl","password":"Clave2026!","nombre":"Dos","rol":"ADMIN"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(cuerpo).get("id").asLong();
        mvc.perform(delete("/api/usuarios/" + id).header(HttpHeaders.AUTHORIZATION, t))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/usuarios/" + id).header(HttpHeaders.AUTHORIZATION, t))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void erroresDeFormatoRespondenJson() throws Exception {
        String t = token("admin");
        mvc.perform(get("/api/usuarios/abc").header(HttpHeaders.AUTHORIZATION, t))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{malo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.xx"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void corsPermiteSoloElOrigenConfigurado() throws Exception {
        mvc.perform(options("/api/auth/login").header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        mvc.perform(options("/api/auth/login").header("Origin", "http://malicioso.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
