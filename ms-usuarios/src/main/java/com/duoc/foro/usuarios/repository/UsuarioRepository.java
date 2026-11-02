package com.duoc.foro.usuarios.repository;

import com.duoc.foro.usuarios.model.Rol;
import com.duoc.foro.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByRol(Rol rol);

    /** Busca por coincidencia parcial en usuario, nombre o correo; el patrón nunca es nulo ("%" = todos). */
    @Query("""
            select u from Usuario u
            where (lower(u.username) like :patron escape '\\'
                or lower(u.nombre) like :patron escape '\\'
                or lower(u.email) like :patron escape '\\')
              and (:rol is null or u.rol = :rol)
            order by u.username
            """)
    List<Usuario> buscar(@Param("patron") String patron, @Param("rol") Rol rol);
}
