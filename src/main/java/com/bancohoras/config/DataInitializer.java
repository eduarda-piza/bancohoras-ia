package com.bancohoras.config;

import com.bancohoras.model.*;
import com.bancohoras.model.enums.*;
import com.bancohoras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PerfilRepository          perfilRepository;
    private final UsuarioRepository         usuarioRepository;
    private final FuncionarioRepository     funcionarioRepository;
    private final BancoHorasRepository      bancoHorasRepository;
    private final RegistroPontoRepository   registroPontoRepository;
    private final NotificacaoRepository     notificacaoRepository;
    private final CompensacaoFolgaRepository compensacaoFolgaRepository;
    private final PasswordEncoder           passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("DataInitializer: banco já populado — seed ignorado.");
            return;
        }

        log.info("DataInitializer: criando dados de demonstração...");

        Perfil admin     = perfil("ADMIN",       "Acesso total ao sistema");
        Perfil gestor    = perfil("GESTOR",       "Gerencia equipes e aprova registros");
        Perfil rh        = perfil("RH",           "Gestão de pessoas e relatórios");
        Perfil funcPerfil = perfil("FUNCIONARIO", "Registra ponto e consulta próprio saldo");

        Usuario adminUser  = usuario("Super Admin",   "admin@bancohoras.com",      "admin123",  admin);
        Usuario gestorUser = usuario("Carlos Mendes", "carlos.mendes@empresa.com", "gestor123", gestor);
        Usuario rhUser     = usuario("Paula Reis",    "paula.rh@empresa.com",      "rh123",     rh);
        usuario("João Ferreira", "joao.f@empresa.com", "joao123", funcPerfil);

        // ── Funcionários ────────────────────────────────────────────────────────

        Funcionario rafael   = func("Rafael Souza",   "Dev Sênior",           "rafael.souza@empresa.com",  "09:00", "18:00");
        Funcionario ana      = func("Ana Lima",        "Analista de Sistemas",  "ana.lima@empresa.com",      "08:00", "17:00");
        Funcionario pedro    = func("Pedro Alves",     "Dev Pleno",             "pedro.alves@empresa.com",   "09:00", "18:00");
        Funcionario carla    = func("Carla Nunes",     "Scrum Master",          "carla.nunes@empresa.com",   "08:00", "17:00");
        Funcionario tatiane  = func("Tatiane Melo",    "UX Designer",           "tatiane.melo@empresa.com",  "08:30", "17:30");
        Funcionario mariana  = func("Mariana Costa",   "Analista de RH",        "mariana.costa@empresa.com", "08:00", "17:00");
        Funcionario felipe   = func("Felipe Santos",   "Dev Júnior",            "felipe.santos@empresa.com", "09:00", "18:00");
        Funcionario lucas    = func("Lucas Teixeira",  "QA Engineer",           "lucas.teixeira@empresa.com","09:00", "18:00");
        Funcionario beatriz  = func("Beatriz Rocha",   "Designer",              "beatriz.rocha@empresa.com", "09:00", "18:00");

        // ── Banco de horas ──────────────────────────────────────────────────────
        banco(rafael,  2880);  // +48h — CRÍTICO
        banco(ana,     1380);  // +23h — ATENÇÃO
        banco(pedro,   1920);  // +32h — ATENÇÃO
        banco(carla,   1080);  // +18h — OK
        banco(tatiane,    0);  // 0    — OK
        banco(mariana,  600);  // +10h — OK
        banco(felipe,   480);  // +8h  — OK
        banco(lucas,    900);  // +15h — OK
        banco(beatriz,  720);  // +12h — OK

        // ── Registros de ponto ─────────────────────────────────────────────────

        // Rafael: ~15 registros, padrão de horas extras
        ponto(rafael, -25, "09:02", "19:15", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael, -24, "09:05", "19:30", StatusRegistro.APROVADO,  false, null,    null);
        ponto(rafael, -23, "09:00", "20:10", StatusRegistro.APROVADO,  true,  "09:00", "Integração com API externa");
        ponto(rafael, -22, "09:10", "18:45", StatusRegistro.APROVADO,  false, null,    null);
        ponto(rafael, -19, "09:00", "21:00", StatusRegistro.APROVADO,  false, null,    "Deploy em produção");
        ponto(rafael, -18, "09:05", "19:00", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael, -17, "09:00", "19:20", StatusRegistro.APROVADO,  false, null,    null);
        ponto(rafael, -16, "09:03", "18:50", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael, -15, "09:00", "19:45", StatusRegistro.APROVADO,  false, null,    "Sprint final");
        ponto(rafael, -12, "09:10", "19:00", StatusRegistro.APROVADO,  false, null,    null);
        ponto(rafael, -11, "09:00", "18:30", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael, -10, "09:00", "19:10", StatusRegistro.APROVADO,  false, null,    null);
        ponto(rafael,  -9, "09:05", "20:30", StatusRegistro.APROVADO,  false, null,    "Correção crítica em produção");
        ponto(rafael,  -2, "09:00", "19:10", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael,  -1, "09:05", "19:00", StatusRegistro.PENDENTE,  false, null,    null);

        // Ana: ~15 registros; registro 12 dias atrás com saída 18:12 (excedeu 10h)
        ponto(ana, -25, "08:05", "17:10", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -24, "08:15", "17:30", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -23, "08:00", "17:00", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -22, "08:20", "17:45", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -19, "08:00", "17:00", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -18, "08:10", "17:20", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -17, "08:00", "18:00", StatusRegistro.APROVADO,  false, null,    "Reunião de alinhamento");
        ponto(ana, -16, "08:05", "17:15", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -15, "08:00", "17:00", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -12, "08:00", "18:12", StatusRegistro.APROVADO,  false, null,    "Ultrapassou 10h de jornada");
        ponto(ana, -11, "08:00", "17:30", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -10, "08:05", "17:05", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana,  -9, "08:00", "17:00", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana,  -5, "08:00", "17:00", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana,  -1, "08:00", "17:00", StatusRegistro.PENDENTE,  false, null,    null);

        // Pedro: ~10 registros, normal 09:00-18:00
        ponto(pedro, -20, "09:00", "18:00", StatusRegistro.APROVADO, false, null,    null);
        ponto(pedro, -19, "09:10", "19:00", StatusRegistro.APROVADO, true,  "09:00", null);
        ponto(pedro, -18, "09:00", "18:30", StatusRegistro.APROVADO, false, null,    null);
        ponto(pedro, -15, "09:00", "19:00", StatusRegistro.APROVADO, false, null,    null);
        ponto(pedro, -12, "09:05", "18:15", StatusRegistro.APROVADO, false, null,    null);
        ponto(pedro, -11, "09:00", "18:00", StatusRegistro.APROVADO, true,  "09:00", null);
        ponto(pedro, -10, "09:00", "18:30", StatusRegistro.APROVADO, false, null,    null);
        ponto(pedro,  -9, "09:10", "18:10", StatusRegistro.APROVADO, false, null,    null);
        ponto(pedro,  -5, "09:00", "18:00", StatusRegistro.APROVADO, false, null,    null);
        ponto(pedro,  -1, "09:05", "18:05", StatusRegistro.PENDENTE, false, null,    null);

        // Carla: ~8 registros, jornada regular
        ponto(carla, -15, "08:00", "17:30", StatusRegistro.APROVADO, false, null,    null);
        ponto(carla, -12, "08:05", "17:05", StatusRegistro.APROVADO, true,  "08:00", null);
        ponto(carla, -11, "08:00", "17:00", StatusRegistro.APROVADO, false, null,    null);
        ponto(carla, -10, "08:00", "17:30", StatusRegistro.APROVADO, false, null,    null);
        ponto(carla,  -9, "08:10", "17:10", StatusRegistro.APROVADO, false, null,    null);
        ponto(carla,  -8, "08:00", "17:15", StatusRegistro.APROVADO, true,  "08:00", null);
        ponto(carla,  -5, "08:00", "17:00", StatusRegistro.APROVADO, false, null,    null);
        ponto(carla,  -2, "08:05", "17:05", StatusRegistro.APROVADO, false, null,    null);

        // Tatiane: ~8 registros, jornada equilibrada
        ponto(tatiane, -10, "08:30", "17:30", StatusRegistro.APROVADO, true,  "08:30", null);
        ponto(tatiane,  -9, "08:35", "17:35", StatusRegistro.APROVADO, false, null,    null);
        ponto(tatiane,  -8, "08:30", "17:30", StatusRegistro.APROVADO, true,  "08:30", null);
        ponto(tatiane,  -5, "08:30", "17:30", StatusRegistro.APROVADO, false, null,    null);
        ponto(tatiane,  -4, "08:30", "17:30", StatusRegistro.APROVADO, true,  "08:30", null);
        ponto(tatiane,  -3, "08:35", "17:35", StatusRegistro.APROVADO, false, null,    null);
        ponto(tatiane,  -2, "08:30", "17:30", StatusRegistro.APROVADO, true,  "08:30", null);
        ponto(tatiane,  -1, "08:30", "17:30", StatusRegistro.APROVADO, false, null,    null);

        // Mariana: registro pendente sugerido pela IA — falta de entrada em 18/03
        pontoSemSaida(mariana, StatusRegistro.PENDENTE, true, "08:00-17:00",
            "Falta de entrada em 18/03 - Sugestão da IA");

        // Felipe: registro pendente — solicitação de folga compensatória
        pontoSemSaida(felipe, StatusRegistro.PENDENTE, true, null,
            "Solicitação de folga compensatória em 02/04");

        // Lucas: registro pendente — falta de saída em 20/03
        pontoSemSaida(lucas, StatusRegistro.PENDENTE, true, "17:30",
            "Falta de saída em 20/03 - Sugestão da IA: 17:30");

        // Beatriz: registro pendente — falta de entrada em 21/03
        pontoSemSaida(beatriz, StatusRegistro.PENDENTE, true, "09:00-18:00",
            "Falta de entrada em 21/03 - Sugestão da IA: 09:00-18:00");

        // ── Notificações para o admin ────────────────────────────────────────────
        notif(adminUser, TipoNotificacao.VENCIMENTO,
            "Rafael Souza tem 48h vencendo em 12 dias. Melhor janela para folga: 31/03 a 04/04.", false);

        notif(adminUser, TipoNotificacao.EXCESSO_JORNADA,
            "Ana Lima ultrapassou 10h hoje. Recomendado encerrar ponto e acionar o RH.", false);

        notif(adminUser, TipoNotificacao.VENCIMENTO,
            "Pedro Alves tem 32h vencendo em 28 dias.", false);

        notif(adminUser, TipoNotificacao.RESUMO_SEMANAL,
            "Semana 23-27/03: 2 excessos de jornada detectados, 1 correção aplicada automaticamente, 3 funcionários com saldo em zona crítica.", true);

        // Notificações extras para gestor e RH
        notif(gestorUser, TipoNotificacao.CORRECAO_PENDENTE,
            "4 registros pendentes de aprovação aguardando revisão da equipe.", false);

        notif(gestorUser, TipoNotificacao.RESUMO_SEMANAL,
            "Sua equipe está com saldo médio de +20h. Rafael Souza (+48h) precisa de folga compensatória.", true);

        notif(rhUser, TipoNotificacao.VENCIMENTO,
            "Rafael Souza: 48h de banco de horas vencendo. Recomende compensação.", false);

        notif(rhUser, TipoNotificacao.RESUMO_SEMANAL,
            "Relatório RH: 4 pendências de aprovação, taxa de conformidade 94%.", true);

        // ── Compensações de folga ────────────────────────────────────────────────
        compensacao(rafael, gestorUser, LocalDate.now().minusDays(20), 8, StatusCompensacao.APROVADA);
        compensacao(rafael, gestorUser, LocalDate.now().plusDays(5),   8, StatusCompensacao.SOLICITADA);
        compensacao(ana,    gestorUser, LocalDate.now().minusDays(15), 4, StatusCompensacao.APROVADA);
        compensacao(carla,  null,       LocalDate.now().plusDays(3),   4, StatusCompensacao.SOLICITADA);

        log.info("DataInitializer: seed concluído — {} usuários, {} funcionários, " +
                 "{} registros de ponto, {} notificações, {} compensações.",
            usuarioRepository.count(), funcionarioRepository.count(),
            registroPontoRepository.count(), notificacaoRepository.count(),
            compensacaoFolgaRepository.count());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private Perfil perfil(String nome, String desc) {
        return perfilRepository.findByNome(nome).orElseGet(() ->
            perfilRepository.save(Perfil.builder().nome(nome).descricao(desc).build()));
    }

    private Usuario usuario(String nome, String email, String senha, Perfil perfil) {
        return usuarioRepository.save(Usuario.builder()
            .nome(nome).email(email.toLowerCase())
            .senhaHash(passwordEncoder.encode(senha))
            .perfil(perfil).status(StatusUsuario.ATIVO).ativo(true)
            .build());
    }

    private Funcionario func(String nome, String cargo, String email,
                              String entrada, String saida) {
        return funcionarioRepository.save(Funcionario.builder()
            .nome(nome).cargo(cargo).email(email)
            .jornadaDiaria(8).horarioEntrada(entrada)
            .horarioSaida(saida).intervalo("1h").ativo(true)
            .build());
    }

    private void banco(Funcionario f, int saldoMinutos) {
        bancoHorasRepository.save(BancoHoras.builder()
            .funcionario(f).saldoAtualMinutos(saldoMinutos)
            .dataVencimento(LocalDate.now().plusYears(1))
            .build());
    }

    private void ponto(Funcionario f, int diasAtras,
                       String entradaStr, String saidaStr,
                       StatusRegistro status, boolean iaAtivo,
                       String horarioIA, String obs) {
        LocalDate data = LocalDate.now().plusDays(diasAtras);
        // Pula finais de semana
        while (data.getDayOfWeek().getValue() >= 6) {
            data = data.minusDays(1);
        }
        LocalDateTime entrada = LocalDateTime.of(data, LocalTime.parse(entradaStr));
        LocalDateTime saida   = LocalDateTime.of(data, LocalTime.parse(saidaStr));
        int duracao = (int) java.time.Duration.between(entrada, saida).toMinutes();

        registroPontoRepository.save(RegistroPonto.builder()
            .funcionario(f).entrada(entrada).saida(saida)
            .duracaoMinutos(duracao).status(status)
            .sugeridoPelaIA(iaAtivo).horarioSugeridoIA(horarioIA)
            .observacao(obs)
            .build());
    }

    private void pontoSemSaida(Funcionario f, StatusRegistro status,
                                boolean iaAtivo, String horarioIA, String obs) {
        LocalDate data = LocalDate.now().minusDays(3);
        while (data.getDayOfWeek().getValue() >= 6) {
            data = data.minusDays(1);
        }
        LocalDateTime entrada = LocalDateTime.of(data, LocalTime.of(9, 0));
        registroPontoRepository.save(RegistroPonto.builder()
            .funcionario(f).entrada(entrada).saida(null)
            .duracaoMinutos(0).status(status)
            .sugeridoPelaIA(iaAtivo).horarioSugeridoIA(horarioIA)
            .observacao(obs)
            .build());
    }

    private void notif(Usuario dest, TipoNotificacao tipo, String msg, boolean lida) {
        notificacaoRepository.save(Notificacao.builder()
            .destinatario(dest).tipo(tipo).mensagem(msg).lida(lida)
            .build());
    }

    private void compensacao(Funcionario f, Usuario aprovadoPor,
                              LocalDate dataFolga, int horas,
                              StatusCompensacao status) {
        compensacaoFolgaRepository.save(CompensacaoFolga.builder()
            .funcionario(f).aprovadoPor(aprovadoPor)
            .dataFolga(dataFolga).horasCompensadas(horas)
            .status(status)
            .build());
    }
}
