package com.duoc.foro.foros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TEMAS")
@Getter
@Setter
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITULO", nullable = false, length = 150)
    private String titulo;

    @Column(name = "CONTENIDO", nullable = false, length = 1000)
    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CATEGORIA_ID", nullable = false)
    private Categoria categoria;

    @Column(name = "AUTOR_ID", nullable = false, updatable = false)
    private Long autorId;

    @Column(name = "AUTOR_USERNAME", nullable = false, updatable = false, length = 50)
    private String autorUsername;

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
