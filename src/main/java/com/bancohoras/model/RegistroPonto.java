package com.bancohoras.model;

import com.bancohoras.model.enums.StatusRegistro;
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
@Table(name = "registros_ponto")
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(nullable = false)
    private LocalDateTime entrada;

    /** Nulo enquanto o funcionário ainda não registrou a saída */
    private LocalDateTime saida;

    /** Calculado ao registrar saída: (saida - entrada) em minutos */
    private int duracaoMinutos;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusRegistro status = StatusRegistro.PENDENTE;

    /** True quando a sugestão de horário veio da IA */
    @Builder.Default
    @Column(nullable = false)
    private boolean sugeridoPelaIA = false;

    /** Horário que a IA sugeriu, ex: "08:00-17:00" */
    @Column(length = 20)
    private String horarioSugeridoIA;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataCriacao;
}
