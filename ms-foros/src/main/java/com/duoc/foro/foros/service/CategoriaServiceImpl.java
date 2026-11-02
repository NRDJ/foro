package com.duoc.foro.foros.service;

import com.duoc.foro.foros.dto.CategoriaRequest;
import com.duoc.foro.foros.dto.CategoriaResponse;
import com.duoc.foro.foros.exception.ConflictoException;
import com.duoc.foro.foros.exception.RecursoNoEncontradoException;
import com.duoc.foro.foros.model.Categoria;
import com.duoc.foro.foros.repository.CategoriaRepository;
import com.duoc.foro.foros.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repository;
    private final TemaRepository temaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.listarConTotalDeTemas();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return repository.obtenerConTotalDeTemas(id).orElseThrow(() -> noExiste(id));
    }

    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest r) {
        String nombre = r.nombre().trim();
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una categoria con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(r.descripcion().trim());
        Categoria guardada = repository.saveAndFlush(categoria);
        log.info("Categoria creada: {}", guardada.getNombre());
        return new CategoriaResponse(guardada.getId(), guardada.getNombre(), guardada.getDescripcion(), 0);
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest r) {
        Categoria categoria = repository.findById(id).orElseThrow(() -> noExiste(id));
        String nombre = r.nombre().trim();
        if (repository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe una categoria con ese nombre");
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(r.descripcion().trim());
        repository.saveAndFlush(categoria);
        log.info("Categoria {} actualizada", id);
        return obtener(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = repository.findById(id).orElseThrow(() -> noExiste(id));
        if (temaRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar una categoria que tiene temas");
        }
        repository.delete(categoria);
        log.info("Categoria {} eliminada", id);
    }

    private RecursoNoEncontradoException noExiste(Long id) {
        return new RecursoNoEncontradoException("No existe la categoria con id " + id);
    }
}
