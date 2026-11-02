package com.duoc.foro.usuarios.service;

import com.duoc.foro.usuarios.dto.LoginRequest;
import com.duoc.foro.usuarios.dto.LoginResponse;
import com.duoc.foro.usuarios.dto.RegistroRequest;
import com.duoc.foro.usuarios.dto.UsuarioCreateRequest;
import com.duoc.foro.usuarios.dto.UsuarioResponse;
import com.duoc.foro.usuarios.dto.UsuarioUpdateRequest;
import com.duoc.foro.usuarios.model.Rol;
import com.duoc.foro.usuarios.security.UsuarioActual;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse registrar(RegistroRequest request);

    LoginResponse login(LoginRequest request);

    List<UsuarioResponse> buscar(String texto, Rol rol);

    UsuarioResponse obtener(Long id, UsuarioActual actual);

    UsuarioResponse crear(UsuarioCreateRequest request);

    UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request, UsuarioActual actual);

    void eliminar(Long id, UsuarioActual actual);
}
