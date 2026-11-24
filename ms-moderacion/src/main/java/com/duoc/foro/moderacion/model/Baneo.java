package com.duoc.foro.moderacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Registro de un tema o comentario ocultado por un administrador. Revertir el baneo elimina el registro. */
@Entity
@Table(name = "BANEOS", uniqueConstraints = @UniqueConstraint(columnNames = {"TIPO", "CONTENIDO_ID"}))
@Getter
@Setter
public class Baneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "TIPO", nullable = false, updatable = false, length = 12)
    private TipoContenido tipo;

    @Column(name = "CONTENIDO_ID", nullable = false, updatable = false)
    private Long contenidoId;

    @Column(name = "MOTIVO", nullable = false, length = 255)
    private String motivo;

    @Column(name = "MODERADOR_ID", nullable = false)
    private Long moderadorId;

    @Column(name = "MODERADOR_USERNAME", nullable = false, length = 50)
    private String moderadorUsername;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void alCrear() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }
}
