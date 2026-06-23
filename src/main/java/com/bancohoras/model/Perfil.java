package com.bancohoras.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfis")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    /** Valores possíveis: FUNCIONARIO, GESTOR, ADMIN, RH */
    @Column(unique = true, nullable = false, length = 30)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;
}
