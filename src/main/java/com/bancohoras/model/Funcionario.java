package com.bancohoras.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    private String cargo;

    private String email;

    /** Horas de jornada diária, ex: 8 */
    @Column(nullable = false)
    private int jornadaDiaria;

    /** Formato "HH:mm", ex: "08:00" */
    @Column(length = 10)
    private String horarioEntrada;

    /** Formato "HH:mm", ex: "17:00" */
    @Column(length = 10)
    private String horarioSaida;

    /** Ex: "1h" */
    @Column(length = 20)
    private String intervalo;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataCriacao;
}
