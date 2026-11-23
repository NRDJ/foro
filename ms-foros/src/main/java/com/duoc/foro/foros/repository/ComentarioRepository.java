package com.duoc.foro.foros.repository;

import com.duoc.foro.foros.model.Comentario;
import com.duoc.foro.foros.model.EstadoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    interface TotalPorTema {
        Long getTemaId();

        long getTotal();
    }

    @Query("select c from Comentario c join fetch c.tema where c.id = :id")
    Optional<Comentario> findWithTemaById(@Param("id") Long id);

    @Query("select c from Comentario c join fetch c.tema where c.tema.id = :temaId order by c.fechaCreacion, c.id")
    List<Comentario> listarPorTema(@Param("temaId") Long temaId);

    @Query("select c from Comentario c join fetch c.tema where c.tema.id = :temaId and c.estado = :estado order by c.fechaCreacion, c.id")
    List<Comentario> listarPorTemaYEstado(@Param("temaId") Long temaId, @Param("estado") EstadoContenido estado);

    long countByTemaIdAndEstado(Long temaId, EstadoContenido estado);

    /** Una sola consulta para los totales de toda una página de temas. */
    @Query("""
            select c.tema.id as temaId, count(c) as total
            from Comentario c
            where c.tema.id in :temaIds and c.estado = :estado
            group by c.tema.id
            """)
    List<TotalPorTema> contarPorTemas(@Param("temaIds") Collection<Long> temaIds, @Param("estado") EstadoContenido estado);
}
