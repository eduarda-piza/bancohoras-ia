package com.bancohoras.service;

import com.bancohoras.model.Funcionario;
import com.bancohoras.model.Notificacao;
import com.bancohoras.model.RegistroPonto;
import com.bancohoras.model.Usuario;
import com.bancohoras.model.enums.StatusRegistro;
import com.bancohoras.model.enums.TipoNotificacao;
import com.bancohoras.repository.BancoHorasRepository;
import com.bancohoras.repository.FuncionarioRepository;
import com.bancohoras.repository.NotificacaoRepository;
import com.bancohoras.repository.RegistroPontoRepository;
import com.bancohoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SistemaIAService {

    private static final int LIMITE_EXCESSO_MINUTOS = 480; // 8 horas
    private static final int DIAS_ALERTA_VENCIMENTO  = 45;
    private static final List<String> PERFIS_GESTORES = List.of("GESTOR", "ADMIN", "RH");
    private static final DateTimeFormatter HORARIO_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final FuncionarioRepository  funcionarioRepository;
    private final RegistroPontoRepository registroPontoRepository;
    private final BancoHorasRepository   bancoHorasRepository;
    private final NotificacaoRepository  notificacaoRepository;
    private final UsuarioRepository      usuarioRepository;

    // -------------------------------------------------------------------------
    // Detecção de falhas de ponto
    // -------------------------------------------------------------------------

    /**
     * Varre todos os funcionários ativos e verifica se há registro de entrada
     * sem saída após o horário padrão de saída do funcionário.
     * Para cada falha encontrada, cria um RegistroPonto com sugeridoPelaIA=true
     * e a saída sugerida igual ao horário padrão do funcionário.
     */
    @Transactional
    public void detectarFalhas() {
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
        List<Funcionario> ativos = funcionarioRepository.findAllByAtivoTrue();

        for (Funcionario funcionario : ativos) {
            LocalTime horarioSaidaPadrao = parseHorario(funcionario.getHorarioSaida());
            if (horarioSaidaPadrao == null) continue;

            // Só age se já passou o horário de saída previsto
            if (LocalTime.now().isBefore(horarioSaidaPadrao)) continue;

            List<RegistroPonto> registrosAbertos = registroPontoRepository
                .findRegistrosAbertosDesde(funcionario.getId(), inicioDoDia);

            for (RegistroPonto aberto : registrosAbertos) {
                LocalDateTime saidaSugerida = LocalDate.now().atTime(horarioSaidaPadrao);
                int duracaoSugerida = (int) java.time.Duration
                    .between(aberto.getEntrada(), saidaSugerida)
                    .toMinutes();

                RegistroPonto sugestao = RegistroPonto.builder()
                    .funcionario(funcionario)
                    .entrada(aberto.getEntrada())
                    .saida(saidaSugerida)
                    .duracaoMinutos(Math.max(duracaoSugerida, 0))
                    .status(StatusRegistro.PENDENTE)
                    .sugeridoPelaIA(true)
                    .horarioSugeridoIA(funcionario.getHorarioSaida())
                    .observacao("Saída sugerida pela IA — registro original sem saída após horário padrão")
                    .build();

                registroPontoRepository.save(sugestao);
                log.info("IA: sugestão de saída criada para funcionário {} em {}", funcionario.getNome(), saidaSugerida);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Alertas de vencimento
    // -------------------------------------------------------------------------

    /**
     * Busca todos os BancoHoras cujo vencimento ocorre nos próximos 45 dias
     * e cria uma Notificacao do tipo VENCIMENTO para cada gestor/admin ativo.
     */
    @Transactional
    public void verificarVencimentos() {
        LocalDate hoje  = LocalDate.now();
        LocalDate limite = hoje.plusDays(DIAS_ALERTA_VENCIMENTO);

        List<Usuario> gestores = usuarioRepository.findByPerfil_NomeIn(PERFIS_GESTORES);
        if (gestores.isEmpty()) {
            log.warn("IA: nenhum gestor/admin encontrado para receber alertas de vencimento.");
            return;
        }

        bancoHorasRepository.findByDataVencimentoBetween(hoje, limite).forEach(banco -> {
            String mensagem = String.format(
                "Atenção: o banco de horas de %s vence em %s (%d min acumulados). "
                + "Providencie a compensação antes do prazo.",
                banco.getFuncionario().getNome(),
                banco.getDataVencimento(),
                banco.getSaldoAtualMinutos()
            );

            gestores.forEach(gestor -> {
                Notificacao notificacao = Notificacao.builder()
                    .destinatario(gestor)
                    .tipo(TipoNotificacao.VENCIMENTO)
                    .mensagem(mensagem)
                    .build();
                notificacaoRepository.save(notificacao);
            });

            log.info("IA: alerta de vencimento criado para funcionário {}", banco.getFuncionario().getNome());
        });
    }

    // -------------------------------------------------------------------------
    // Alertas de excesso de jornada
    // -------------------------------------------------------------------------

    /**
     * Verifica registros do dia atual com duração acima de 8 horas e cria
     * uma Notificacao do tipo EXCESSO_JORNADA para cada gestor ativo.
     */
    @Transactional
    public void verificarExcessoJornada() {
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();

        List<Usuario> gestores = usuarioRepository.findByPerfil_NomeIn(PERFIS_GESTORES);
        if (gestores.isEmpty()) {
            log.warn("IA: nenhum gestor/admin encontrado para receber alertas de excesso de jornada.");
            return;
        }

        List<RegistroPonto> excessos = registroPontoRepository
            .findExcessosJornada(LIMITE_EXCESSO_MINUTOS, inicioDoDia);

        for (RegistroPonto registro : excessos) {
            int horasExtras = (registro.getDuracaoMinutos() - LIMITE_EXCESSO_MINUTOS) / 60;
            int minutosExtras = (registro.getDuracaoMinutos() - LIMITE_EXCESSO_MINUTOS) % 60;

            String mensagem = String.format(
                "Excesso de jornada detectado: %s trabalhou %dh%02dmin além do limite hoje (%s às %s).",
                registro.getFuncionario().getNome(),
                horasExtras,
                minutosExtras,
                registro.getEntrada().toLocalDate(),
                registro.getEntrada().toLocalTime().format(HORARIO_FMT)
            );

            gestores.forEach(gestor -> {
                Notificacao notificacao = Notificacao.builder()
                    .destinatario(gestor)
                    .tipo(TipoNotificacao.EXCESSO_JORNADA)
                    .mensagem(mensagem)
                    .build();
                notificacaoRepository.save(notificacao);
            });

            log.info("IA: alerta de excesso de jornada criado para funcionário {}", registro.getFuncionario().getNome());
        }
    }

    // -------------------------------------------------------------------------
    // Utilitário interno
    // -------------------------------------------------------------------------

    private LocalTime parseHorario(String horario) {
        if (horario == null || horario.isBlank()) return null;
        try {
            return LocalTime.parse(horario.trim(), HORARIO_FMT);
        } catch (DateTimeParseException e) {
            log.warn("IA: horário inválido '{}' — ignorado na análise.", horario);
            return null;
        }
    }
}
