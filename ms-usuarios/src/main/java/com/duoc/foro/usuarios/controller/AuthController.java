package com.duoc.foro.usuarios.controller;

import com.duoc.foro.usuarios.dto.LoginRequest;
import com.duoc.foro.usuarios.dto.LoginResponse;
import com.duoc.foro.usuarios.dto.RegistroRequest;
import com.duoc.foro.usuarios.dto.UsuarioResponse;
import com.duoc.foro.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.origen}")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService service;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
