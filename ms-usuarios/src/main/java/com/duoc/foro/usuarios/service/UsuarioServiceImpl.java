package com.duoc.foro.usuarios.service;

import com.duoc.foro.usuarios.dto.LoginRequest;
import com.duoc.foro.usuarios.dto.LoginResponse;
import com.duoc.foro.usuarios.dto.RegistroRequest;
import com.duoc.foro.usuarios.dto.UsuarioCreateRequest;
import com.duoc.foro.usuarios.dto.UsuarioResponse;
import com.duoc.foro.usuarios.dto.UsuarioUpdateRequest;
import com.duoc.foro.usuarios.exception.ConflictoException;
import com.duoc.foro.usuarios.exception.CredencialesInvalidasException;
import com.duoc.foro.usuarios.exception.RecursoNoEncontradoException;
import com.duoc.foro.usuarios.exception.SolicitudInvalidaException;
import com.duoc.foro.usuarios.model.Rol;
import com.duoc.foro.usuarios.model.Usuario;
import com.duoc.foro.usuarios.repository.UsuarioRepository;
import com.duoc.foro.usuarios.security.TokenService;
import com.duoc.foro.usuarios.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistroRequest r) {
        // El registro público siempre crea usuarios comunes: un ADMIN solo lo puede crear otro ADMIN.
        return guardarNuevo(r.username(), r.email(), r.password(), r.nombre(), Rol.USUARIO);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest r) {
        Usuario usuario = repository.findByUsername(normalizar(r.username()))
                .filter(u -> passwordEncoder.matches(r.password(), u.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Inicio de sesión fallido para '{}'", r.username());
                    return new CredencialesInvalidasException();
                });
        log.info("Inicio de sesión correcto: {} ({})", usuario.getUsername(), usuario.getRol());
        return new LoginResponse(tokenService.generar(usuario), "Bearer", tokenService.segundosDeVigencia(),
                UsuarioResponse.desde(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscar(String texto, Rol rol) {
        return repository.buscar(patronLike(texto), rol).stream().map(UsuarioResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id, UsuarioActual actual) {
        exigirAdminOTitular(id, actual);
        return UsuarioResponse.desde(buscarPorId(id));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest r) {
        return guardarNuevo(r.username(), r.email(), r.password(), r.nombre(), r.rol());
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest r, UsuarioActual actual) {
        exigirAdminOTitular(id, actual);
        Usuario usuario = buscarPorId(id);

        if (r.rol() != null && r.rol() != usuario.getRol()) {
            if (!actual.esAdmin()) {
                throw new AccessDeniedException("Solo un administrador puede cambiar el rol");
            }
            exigirOtroAdmin(usuario);
            usuario.setRol(r.rol());
        }

        String email = normalizar(r.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        usuario.setEmail(email);
        usuario.setNombre(r.nombre().trim());
        if (r.password() != null) {
            usuario.setPasswordHash(codificarClave(r.password()));
        }

        log.info("Usuario {} actualizado por {}", usuario.getUsername(), actual.username());
        return UsuarioResponse.desde(repository.saveAndFlush(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id, UsuarioActual actual) {
        exigirAdminOTitular(id, actual);
        Usuario usuario = buscarPorId(id);
        exigirOtroAdmin(usuario);
        repository.delete(usuario);
        log.info("Usuario {} eliminado por {}", usuario.getUsername(), actual.username());
    }

    private UsuarioResponse guardarNuevo(String username, String email, String password, String nombre, Rol rol) {
        String usernameNormalizado = normalizar(username);
        String emailNormalizado = normalizar(email);
        if (repository.existsByUsername(usernameNormalizado)) {
            throw new ConflictoException("El nombre de usuario ya está registrado");
        }
        if (repository.existsByEmail(emailNormalizado)) {
            throw new ConflictoException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usernameNormalizado);
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(codificarClave(password));
        usuario.setNombre(nombre.trim());
        usuario.setRol(rol);

        Usuario guardado = repository.saveAndFlush(usuario);
        log.info("Usuario creado: {} ({})", guardado.getUsername(), guardado.getRol());
        return UsuarioResponse.desde(guardado);
    }

    /** BCrypt admite hasta 72 bytes; una clave con acentos o emojis puede superarlos con menos de 72 caracteres. */
    private String codificarClave(String clave) {
        if (clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new SolicitudInvalidaException("La contraseña es demasiado larga");
        }
        return passwordEncoder.encode(clave);
    }

    private Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private void exigirAdminOTitular(Long id, UsuarioActual actual) {
        if (!actual.esAdmin() && !actual.id().equals(id)) {
            throw new AccessDeniedException("Solo puede acceder a su propia cuenta");
        }
    }

    /** Evita que el sistema quede sin ningún administrador. */
    private void exigirOtroAdmin(Usuario usuario) {
        if (usuario.getRol() == Rol.ADMIN && repository.countByRol(Rol.ADMIN) <= 1) {
            throw new ConflictoException("No se puede quitar al único administrador del sistema");
        }
    }

    private String normalizar(String valor) {
        return valor.trim().toLowerCase(Locale.ROOT);
    }

    private String patronLike(String texto) {
        if (texto == null || texto.isBlank()) {
            return "%";
        }
        String escapado = normalizar(texto).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
