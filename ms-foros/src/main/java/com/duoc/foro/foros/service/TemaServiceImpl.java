package com.duoc.foro.foros.service;

import com.duoc.foro.foros.dto.ComentarioResponse;
import com.duoc.foro.foros.dto.PaginaResponse;
import com.duoc.foro.foros.dto.TemaDetalleResponse;
import com.duoc.foro.foros.dto.TemaRequest;
import com.duoc.foro.foros.dto.TemaResponse;
import com.duoc.foro.foros.exception.RecursoNoEncontradoException;
import com.duoc.foro.foros.model.Categoria;
import com.duoc.foro.foros.model.Tema;
import com.duoc.foro.foros.repository.CategoriaRepository;
import com.duoc.foro.foros.repository.ComentarioRepository;
import com.duoc.foro.foros.repository.TemaRepository;
import com.duoc.foro.foros.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemaServiceImpl implements TemaService {

    private final TemaRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final ComentarioRepository comentarioRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<TemaResponse> buscar(String texto, Long categoriaId, Long autorId, int pagina, int tamano) {
        Sort orden = Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.desc("id"));
        Page<Tema> temas = repository.buscar(patronLike(texto), categoriaId, autorId, PageRequest.of(pagina, tamano, orden));

        Map<Long, Long> totales = temas.isEmpty() ? Map.of()
                : comentarioRepository.contarPorTemas(temas.map(Tema::getId).getContent()).stream()
                .collect(Collectors.toMap(ComentarioRepository.TotalPorTema::getTemaId, ComentarioRepository.TotalPorTema::getTotal));

        List<TemaResponse> contenido = temas.getContent().stream()
                .map(t -> TemaResponse.desde(t, totales.getOrDefault(t.getId(), 0L)))
                .toList();
        return new PaginaResponse<>(contenido, temas.getNumber(), temas.getSize(), temas.getTotalElements(), temas.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public TemaDetalleResponse obtenerDetalle(Long id) {
        Tema tema = buscarConCategoria(id);
        List<ComentarioResponse> comentarios = comentarioRepository.listarPorTema(id).stream()
                .map(ComentarioResponse::desde)
                .toList();
        return new TemaDetalleResponse(TemaResponse.desde(tema, comentarios.size()), comentarios);
    }

    @Override
    @Transactional
    public TemaResponse publicar(TemaRequest r, UsuarioActual autor) {
        Tema tema = new Tema();
        tema.setTitulo(r.titulo().trim());
        tema.setContenido(r.contenido().trim());
        tema.setCategoria(buscarCategoria(r.categoriaId()));
        tema.setAutorId(autor.id());
        tema.setAutorUsername(autor.username());
        Tema guardado = repository.saveAndFlush(tema);
        log.info("Tema {} publicado por {}", guardado.getId(), autor.username());
        return TemaResponse.desde(guardado, 0);
    }

    @Override
    @Transactional
    public TemaResponse actualizar(Long id, TemaRequest r, UsuarioActual actual) {
        Tema tema = buscarConCategoria(id);
        actual.exigirAutorOAdmin(tema.getAutorId());
        tema.setTitulo(r.titulo().trim());
        tema.setContenido(r.contenido().trim());
        tema.setCategoria(buscarCategoria(r.categoriaId()));
        Tema guardado = repository.saveAndFlush(tema);
        log.info("Tema {} actualizado por {}", id, actual.username());
        return TemaResponse.desde(guardado, comentarioRepository.countByTemaId(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id, UsuarioActual actual) {
        Tema tema = buscarConCategoria(id);
        actual.exigirAutorOAdmin(tema.getAutorId());
        repository.delete(tema);
        log.info("Tema {} eliminado por {}", id, actual.username());
    }

    private Tema buscarConCategoria(Long id) {
        return repository.findWithCategoriaById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tema con id " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoria con id " + id));
    }

    private String patronLike(String texto) {
        if (texto == null || texto.isBlank()) {
            return "%";
        }
        String escapado = texto.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
