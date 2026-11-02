package com.duoc.foro.foros.repository;

import com.duoc.foro.foros.model.Tema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {

    boolean existsByCategoriaId(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    Optional<Tema> findWithCategoriaById(Long id);

    /**
     * Búsqueda combinable por texto (título o contenido), categoría y autor.
     * El patrón nunca es nulo ("%" = todos); los filtros nulos no se aplican.
     */
    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select t from Tema t
            where (lower(t.titulo) like :patron escape '\\' or lower(t.contenido) like :patron escape '\\')
              and (:categoriaId is null or t.categoria.id = :categoriaId)
              and (:autorId is null or t.autorId = :autorId)
            """)
    Page<Tema> buscar(@Param("patron") String patron,
                      @Param("categoriaId") Long categoriaId,
                      @Param("autorId") Long autorId,
                      Pageable pageable);
}
