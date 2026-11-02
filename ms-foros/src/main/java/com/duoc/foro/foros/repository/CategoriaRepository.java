package com.duoc.foro.foros.repository;

import com.duoc.foro.foros.dto.CategoriaResponse;
import com.duoc.foro.foros.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    @Query("""
            select new com.duoc.foro.foros.dto.CategoriaResponse(c.id, c.nombre, c.descripcion, count(t))
            from Categoria c left join Tema t on t.categoria = c
            group by c.id, c.nombre, c.descripcion
            order by c.nombre
            """)
    List<CategoriaResponse> listarConTotalDeTemas();

    @Query("""
            select new com.duoc.foro.foros.dto.CategoriaResponse(c.id, c.nombre, c.descripcion, count(t))
            from Categoria c left join Tema t on t.categoria = c
            where c.id = :id
            group by c.id, c.nombre, c.descripcion
            """)
    Optional<CategoriaResponse> obtenerConTotalDeTemas(@Param("id") Long id);
}
