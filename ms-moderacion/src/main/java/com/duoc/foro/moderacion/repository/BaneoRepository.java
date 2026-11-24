package com.duoc.foro.moderacion.repository;

import com.duoc.foro.moderacion.model.Baneo;
import com.duoc.foro.moderacion.model.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaneoRepository extends JpaRepository<Baneo, Long> {

    boolean existsByTipoAndContenidoId(TipoContenido tipo, Long contenidoId);

    /** Lista los baneos más recientes primero; si el tipo es nulo, trae todos. */
    @Query("""
            select b from Baneo b
            where (:tipo is null or b.tipo = :tipo)
            order by b.fechaCreacion desc, b.id desc
            """)
    List<Baneo> listar(@Param("tipo") TipoContenido tipo);
}
