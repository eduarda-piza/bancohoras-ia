package com.bancohoras.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LancamentoHorasDTO {

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    private LocalTime horaEntrada;

    private LocalTime horaSaida;

    private String tipo;

    private String observacao;
}
