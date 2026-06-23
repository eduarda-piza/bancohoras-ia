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

        Funcionario rafael  = func("Rafael Souza",   "Desenvolvedor Sênior", "rafael.souza@empresa.com",  "09:00", "18:00");
        Funcionario ana     = func("Ana Lima",        "Analista de Sistemas",  "ana.lima@empresa.com",      "08:00", "17:00");
        Funcionario pedro   = func("Pedro Alves",     "Desenvolvedor Pleno",   "pedro.alves@empresa.com",   "09:00", "18:00");
        Funcionario carla   = func("Carla Nunes",     "Scrum Master",          "carla.nunes@empresa.com",   "08:00", "17:00");
        Funcionario tatiane = func("Tatiane Melo",    "UX Designer",           "tatiane.melo@empresa.com",  "08:30", "17:30");
        Funcionario marcos  = func("Marcos Oliveira", "DevOps Engineer",       "marcos.o@empresa.com",      "09:00", "18:00");
        Funcionario julia   = func("Júlia Costa",     "Product Manager",       "julia.costa@empresa.com",   "08:00", "17:00");

        // ── Banco de horas ──────────────────────────────────────────────────────
        //                      saldo em minutos   (positivo = horas extras, negativo = débito)
        banco(rafael,   3120);  // +52h — muitas horas extras acumuladas
        banco(ana,      1380);  // +23h
        banco(pedro,    1980);  // +33h
        banco(carla,     720);  // +12h
        banco(tatiane,     0);  // zerado — jornada equilibrada
        banco(marcos,   2640);  // +44h
        banco(julia,    -480);  // -8h — deve horas (foi compensar antes de acumular)

        // ── Registros de ponto (últimos 30 dias úteis) ──────────────────────────

        // Rafael: padrão de horas extras quase todo dia
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
        ponto(rafael,  -8, "09:00", "18:45", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael,  -5, "09:03", "19:15", StatusRegistro.APROVADO,  false, null,    null);
        ponto(rafael,  -4, "09:00", "19:00", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael,  -3, "09:10", "19:30", StatusRegistro.CORRIGIDO, false, null,    "Saída ajustada pelo gestor");
        ponto(rafael,  -2, "09:00", "19:10", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(rafael,  -1, "09:05", "19:00", StatusRegistro.PENDENTE,  false, null,    null);

        // Ana: jornada mais regular, alguns atrasos leves
        ponto(ana, -25, "08:05", "17:10", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -24, "08:15", "17:30", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -23, "08:00", "17:00", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -22, "08:20", "17:45", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -19, "08:00", "17:00", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -18, "08:10", "17:20", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -17, "08:00", "18:00", StatusRegistro.APROVADO,  false, null,    "Reunião de alinhamento");
        ponto(ana, -16, "08:05", "17:15", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -15, "08:00", "17:00", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana, -12, "08:10", "17:10", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -11, "08:00", "17:30", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana, -10, "08:05", "17:05", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana,  -9, "08:00", "17:00", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana,  -8, "08:30", "17:30", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana,  -5, "08:00", "17:00", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana,  -4, "08:15", "17:15", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana,  -3, "08:00", "17:00", StatusRegistro.APROVADO,  false, null,    null);
        ponto(ana,  -2, "08:10", "17:10", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(ana,  -1, "08:00", "17:00", StatusRegistro.PENDENTE,  false, null,    null);

        // Pedro: misto, com uma correção pendente
        ponto(pedro, -20, "09:00", "18:00", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro, -19, "09:10", "19:00", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(pedro, -18, "09:00", "18:30", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro, -17, "09:05", "18:45", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro, -16, "09:00", "18:00", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(pedro, -15, "09:00", "19:00", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro, -12, "09:05", "18:15", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro, -11, "09:00", "18:00", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(pedro, -10, "09:00", "18:30", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro,  -9, "09:10", "18:10", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro,  -8, "09:00", "18:00", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(pedro,  -5, "09:00", "18:00", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro,  -4, "09:05", "18:45", StatusRegistro.CORRIGIDO, false, null, "Entrada corrigida — sistema offline");
        ponto(pedro,  -3, "09:00", "18:30", StatusRegistro.APROVADO,  false, null, null);
        ponto(pedro,  -2, "09:00", "18:00", StatusRegistro.APROVADO,  true,  "09:00", null);
        ponto(pedro,  -1, "09:05", "18:05", StatusRegistro.PENDENTE,  false, null, null);

        // Carla: jornada regular de SM
        ponto(carla, -15, "08:00", "17:30", StatusRegistro.APROVADO,  false, null, null);
        ponto(carla, -12, "08:05", "17:05", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(carla, -11, "08:00", "17:00", StatusRegistro.APROVADO,  false, null, null);
        ponto(carla, -10, "08:00", "17:30", StatusRegistro.APROVADO,  false, null, null);
        ponto(carla,  -9, "08:10", "17:10", StatusRegistro.APROVADO,  false, null, null);
        ponto(carla,  -8, "08:00", "17:15", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(carla,  -5, "08:00", "17:00", StatusRegistro.APROVADO,  false, null, null);
        ponto(carla,  -4, "08:05", "17:05", StatusRegistro.APROVADO,  false, null, null);
        ponto(carla,  -3, "08:00", "17:00", StatusRegistro.APROVADO,  true,  "08:00", null);
        ponto(carla,  -2, "08:00", "17:00", StatusRegistro.APROVADO,  false, null, null);
        ponto(carla,  -1, "08:05", "17:05", StatusRegistro.PENDENTE,  false, null, null);

        // Tatiane: jornada zerada, muito equilibrada
        ponto(tatiane, -10, "08:30", "17:30", StatusRegistro.APROVADO,  true,  "08:30", null);
        ponto(tatiane,  -9, "08:35", "17:35", StatusRegistro.APROVADO,  false, null,    null);
        ponto(tatiane,  -8, "08:30", "17:30", StatusRegistro.APROVADO,  true,  "08:30", null);
        ponto(tatiane,  -5, "08:30", "17:30", StatusRegistro.APROVADO,  false, null,    null);
        ponto(tatiane,  -4, "08:30", "17:30", StatusRegistro.APROVADO,  true,  "08:30", null);
        ponto(tatiane,  -3, "08:35", "17:35", StatusRegistro.APROVADO,  false, null,    null);
        ponto(tatiane,  -2, "08:30", "17:30", StatusRegistro.APROVADO,  true,  "08:30", null);
        ponto(tatiane,  -1, "08:30", "17:30", StatusRegistro.PENDENTE,  false, null,    null);

        // Marcos: muitas extras, DevOps fica de plantão
        ponto(marcos, -15, "09:00", "20:00", StatusRegistro.APROVADO, false, null, "Manutenção do servidor");
        ponto(marcos, -12, "09:05", "19:30", StatusRegistro.APROVADO, true,  "09:00", null);
        ponto(marcos, -11, "09:00", "19:00", StatusRegistro.APROVADO, false, null, null);
        ponto(marcos, -10, "09:00", "20:30", StatusRegistro.APROVADO, false, null, "Migração de banco de dados");
        ponto(marcos,  -9, "09:10", "18:10", StatusRegistro.APROVADO, true,  "09:00", null);
        ponto(marcos,  -8, "09:00", "19:15", StatusRegistro.APROVADO, false, null, null);
        ponto(marcos,  -5, "09:00", "18:00", StatusRegistro.APROVADO, false, null, null);
        ponto(marcos,  -4, "09:05", "19:05", StatusRegistro.APROVADO, true,  "09:00", null);
        ponto(marcos,  -3, "09:00", "19:30", StatusRegistro.APROVADO, false, null, "CI/CD pipeline");
        ponto(marcos,  -2, "09:00", "18:45", StatusRegistro.APROVADO, false, null, null);
        ponto(marcos,  -1, "09:05", "18:05", StatusRegistro.PENDENTE, false, null, null);

        // Júlia: débito de horas (compensou antes)
        ponto(julia, -10, "08:00", "15:00", StatusRegistro.APROVADO, false, null, "Saída antecipada — consulta médica");
        ponto(julia,  -9, "08:05", "17:05", StatusRegistro.APROVADO, true,  "08:00", null);
        ponto(julia,  -8, "08:00", "17:00", StatusRegistro.APROVADO, false, null, null);
        ponto(julia,  -5, "08:00", "15:30", StatusRegistro.APROVADO, false, null, "Evento externo de produto");
        ponto(julia,  -4, "08:10", "17:10", StatusRegistro.APROVADO, true,  "08:00", null);
        ponto(julia,  -3, "08:00", "17:00", StatusRegistro.APROVADO, false, null, null);
        ponto(julia,  -2, "08:05", "17:05", StatusRegistro.APROVADO, false, null, null);
        ponto(julia,  -1, "08:00", "17:00", StatusRegistro.PENDENTE, false, null, null);

        // ── Notificações ────────────────────────────────────────────────────────

        notif(adminUser, TipoNotificacao.EXCESSO_JORNADA,
            "⚠️ Rafael Souza trabalhou 12h no dia " + LocalDate.now().minusDays(9).toString() +
            ". Jornada acima do limite recomendado de 10h.", false);

        notif(adminUser, TipoNotificacao.EXCESSO_JORNADA,
            "⚠️ Marcos Oliveira trabalhou 11h30 no dia " + LocalDate.now().minusDays(10).toString() +
            " (migração de banco de dados). Considere compensação.", false);

        notif(adminUser, TipoNotificacao.VENCIMENTO,
            "📅 Rafael Souza acumulou 52h extras. Saldo vence em " +
            LocalDate.now().plusYears(1).toString() + ". Recomende compensação.", false);

        notif(adminUser, TipoNotificacao.CORRECAO_PENDENTE,
            "🔧 Pedro Alves possui um registro de ponto corrigido em " +
            LocalDate.now().minusDays(4).toString() + " aguardando revisão.", false);

        notif(adminUser, TipoNotificacao.RESUMO_SEMANAL,
            "📊 Resumo semanal: 7 funcionários ativos, 19 registros pendentes de aprovação, " +
            "saldo médio da equipe: +26h. Alertas: 2 jornadas excessivas.", true);

        notif(gestorUser, TipoNotificacao.CORRECAO_PENDENTE,
            "🔧 Pedro Alves possui registro corrigido em " +
            LocalDate.now().minusDays(4).toString() + ". Verificar e aprovar.", false);

        notif(gestorUser, TipoNotificacao.RESUMO_SEMANAL,
            "📊 Sua equipe esta com saldo médio de +28h. Rafael Souza (+52h) e Marcos Oliveira (+44h) " +
            "precisam de folgas compensatórias em breve.", true);

        notif(rhUser, TipoNotificacao.VENCIMENTO,
            "📅 Júlia Costa está com saldo negativo de -8h. Verificar necessidade de reposição.", false);

        notif(rhUser, TipoNotificacao.RESUMO_SEMANAL,
            "📊 Relatório RH: 3 solicitações de compensação no mês, taxa de aprovação 100%. " +
            "Conformidade de jornada: 94% dos registros dentro do esperado.", true);

        // ── Compensações de folga ────────────────────────────────────────────────

        compensacao(rafael, gestorUser,
            LocalDate.now().minusDays(20), 8, StatusCompensacao.APROVADA);

        compensacao(rafael, gestorUser,
            LocalDate.now().plusDays(5), 8, StatusCompensacao.SOLICITADA);

        compensacao(marcos, gestorUser,
            LocalDate.now().minusDays(10), 8, StatusCompensacao.APROVADA);

        compensacao(marcos, null,
            LocalDate.now().plusDays(12), 8, StatusCompensacao.SOLICITADA);

        compensacao(ana, gestorUser,
            LocalDate.now().minusDays(15), 4, StatusCompensacao.APROVADA);

        compensacao(carla, null,
            LocalDate.now().plusDays(3), 4, StatusCompensacao.SOLICITADA);

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
