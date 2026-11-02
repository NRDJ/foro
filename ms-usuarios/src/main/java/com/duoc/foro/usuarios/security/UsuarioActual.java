package com.duoc.foro.usuarios.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Datos del usuario autenticado, obtenidos del token JWT. */
public record UsuarioActual(Long id, String username, String rol) {

    public static UsuarioActual desde(Jwt jwt) {
        return new UsuarioActual(((Number) jwt.getClaim("uid")).longValue(), jwt.getSubject(), jwt.getClaimAsString("rol"));
    }

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }
}
