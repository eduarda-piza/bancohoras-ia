package com.bancohoras.model;

import com.bancohoras.model.enums.TipoRelatorio;
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
@Table(name = "relatorios")
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRelatorio tipo;

    /** Ex: "Maio 2026", "2026-T2", etc. */
    @Column(length = 50)
    private String periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerado_por_id")
    private Usuario geradoPor;

    /** Conteúdo serializado em JSON para armazenar dados do relatório */
    @Column(columnDefinition = "TEXT")
    private String dadosJson;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataCriacao;
}
