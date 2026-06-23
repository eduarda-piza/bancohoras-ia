package com.bancohoras.model;

import com.bancohoras.model.enums.StatusCompensacao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compensacoes_folga")
public class CompensacaoFolga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    /** Gestor/RH que aprovou — pode ser nulo enquanto pendente */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovado_por_id")
    private Usuario aprovadoPor;

    @Column(nullable = false)
    private LocalDate dataFolga;

    /** Quantidade de horas que serão deduzidas do banco */
    @Column(nullable = false)
    private int horasCompensadas;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCompensacao status = StatusCompensacao.SOLICITADA;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataSolicitacao;
}
