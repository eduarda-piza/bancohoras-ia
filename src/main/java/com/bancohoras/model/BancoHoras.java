package com.bancohoras.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "banco_horas")
public class BancoHoras {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    /** Relação 1-para-1: cada funcionário tem exatamente um banco de horas */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false, unique = true)
    private Funcionario funcionario;

    /**
     * Saldo armazenado em minutos para evitar erros de arredondamento.
     * Positivo = horas extras; negativo = horas devidas.
     */
    @Builder.Default
    @Column(nullable = false)
    private int saldoAtualMinutos = 0;

    /** Data limite para uso/compensação das horas acumuladas */
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private LocalDateTime ultimaAtualizacao;

    @PrePersist
    @PreUpdate
    private void atualizarTimestamp() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }
}
