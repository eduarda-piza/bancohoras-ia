package com.bancohoras.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Agendador automático da IA.
 * As tarefas são executadas pelos crons definidos e delegam para SistemaIAService.
 * @EnableScheduling está na classe principal BancoHorasApplication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IAScheduler {

    private final SistemaIAService sistemaIAService;

    /**
     * Todo dia útil às 20h: detecta funcionários que esqueceram de bater saída
     * e cria registros de ponto com sugestão da IA para aprovação dos gestores.
     */
    @Scheduled(cron = "0 0 20 * * MON-FRI")
    public void verificarFalhasDeRegistro() {
        log.info("[IA-Scheduler] Iniciando detecção de falhas de registro de ponto...");
        try {
            sistemaIAService.detectarFalhas();
            log.info("[IA-Scheduler] Detecção de falhas concluída.");
        } catch (Exception e) {
            log.error("[IA-Scheduler] Erro na detecção de falhas: {}", e.getMessage(), e);
        }
    }

    /**
     * Toda segunda-feira às 8h: verifica bancos de horas com vencimento
     * nos próximos 45 dias e notifica os gestores.
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void verificarVencimentos() {
        log.info("[IA-Scheduler] Verificando vencimentos de banco de horas...");
        try {
            sistemaIAService.verificarVencimentos();
            log.info("[IA-Scheduler] Verificação de vencimentos concluída.");
        } catch (Exception e) {
            log.error("[IA-Scheduler] Erro na verificação de vencimentos: {}", e.getMessage(), e);
        }
    }

    /**
     * Todo dia útil às 17h30: detecta jornadas acima de 8h no dia e
     * cria alertas de excesso de jornada para os gestores.
     */
    @Scheduled(cron = "0 30 17 * * MON-FRI")
    public void verificarExcessos() {
        log.info("[IA-Scheduler] Verificando excessos de jornada...");
        try {
            sistemaIAService.verificarExcessoJornada();
            log.info("[IA-Scheduler] Verificação de excessos concluída.");
        } catch (Exception e) {
            log.error("[IA-Scheduler] Erro na verificação de excessos: {}", e.getMessage(), e);
        }
    }
}
