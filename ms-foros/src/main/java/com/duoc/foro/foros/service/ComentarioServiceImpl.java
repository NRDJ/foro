package com.duoc.foro.foros.service;

import com.duoc.foro.foros.dto.ComentarioRequest;
import com.duoc.foro.foros.dto.ComentarioResponse;
import com.duoc.foro.foros.exception.RecursoNoEncontradoException;
import com.duoc.foro.foros.model.Comentario;
import com.duoc.foro.foros.model.Tema;
import com.duoc.foro.foros.repository.ComentarioRepository;
import com.duoc.foro.foros.repository.TemaRepository;
import com.duoc.foro.foros.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository repository;
    private final TemaRepository temaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorTema(Long temaId) {
        buscarTema(temaId);
        return repository.listarPorTema(temaId).stream().map(ComentarioResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ComentarioResponse obtener(Long id) {
        return ComentarioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public ComentarioResponse comentar(Long temaId, ComentarioRequest r, UsuarioActual autor) {
        Comentario comentario = new Comentario();
        comentario.setContenido(r.contenido().trim());
        comentario.setTema(buscarTema(temaId));
        comentario.setAutorId(autor.id());
        comentario.setAutorUsername(autor.username());
        Comentario guardado = repository.saveAndFlush(comentario);
        log.info("Comentario {} agregado al tema {} por {}", guardado.getId(), temaId, autor.username());
        return ComentarioResponse.desde(guardado);
    }

    @Override
    @Transactional
    public ComentarioResponse actualizar(Long id, ComentarioRequest r, UsuarioActual actual) {
        Comentario comentario = buscar(id);
        actual.exigirAutorOAdmin(comentario.getAutorId());
        comentario.setContenido(r.contenido().trim());
        Comentario guardado = repository.saveAndFlush(comentario);
        log.info("Comentario {} actualizado por {}", id, actual.username());
        return ComentarioResponse.desde(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id, UsuarioActual actual) {
        Comentario comentario = buscar(id);
        actual.exigirAutorOAdmin(comentario.getAutorId());
        repository.delete(comentario);
        log.info("Comentario {} eliminado por {}", id, actual.username());
    }

    private Comentario buscar(Long id) {
        return repository.findWithTemaById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el comentario con id " + id));
    }

    private Tema buscarTema(Long id) {
        return temaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tema con id " + id));
    }
}
