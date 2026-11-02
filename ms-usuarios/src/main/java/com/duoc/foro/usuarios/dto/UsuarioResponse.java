package com.duoc.foro.usuarios.dto;

import com.duoc.foro.usuarios.model.Rol;
import com.duoc.foro.usuarios.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String username,
        String email,
        String nombre,
        Rol rol,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail(), u.getNombre(), u.getRol(),
                u.getFechaCreacion(), u.getFechaActualizacion());
    }
}
