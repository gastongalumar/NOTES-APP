package com.notasapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "recordatorios")
@Getter
@Setter
@NoArgsConstructor
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nota_id", nullable = false)
    private Nota nota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "recordar_at", nullable = false)
    private Instant recordarAt;

    @Column(name = "enviado_at")
    private Instant enviadoAt;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
