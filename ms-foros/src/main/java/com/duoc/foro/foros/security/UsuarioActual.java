package com.duoc.foro.foros.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

/** Datos del usuario autenticado, obtenidos del token JWT emitido por ms-usuarios. */
public record UsuarioActual(Long id, String username, String rol) {

    public static UsuarioActual desde(Jwt jwt) {
        return new UsuarioActual(((Number) jwt.getClaim("uid")).longValue(), jwt.getSubject(), jwt.getClaimAsString("rol"));
    }

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }

    /** Un usuario común solo gestiona lo que publicó; un administrador gestiona todo. */
    public void exigirAutorOAdmin(Long autorId) {
        if (!esAdmin() && !id.equals(autorId)) {
            throw new AccessDeniedException("Solo el autor o un administrador puede realizar esta operación");
        }
    }
}
