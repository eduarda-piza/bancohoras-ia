package com.bancohoras.config;

import com.bancohoras.model.*;
import com.bancohoras.model.enums.StatusUsuario;
import com.bancohoras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inicializa dados de demonstração na primeira execução.
 * Idempotente: só age se a tabela de usuários estiver vazia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PerfilRepository       perfilRepository;
    private final UsuarioRepository      usuarioRepository;
    private final FuncionarioRepository  funcionarioRepository;
    private final BancoHorasRepository   bancoHorasRepository;
    private final PasswordEncoder        passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("DataInitializer: banco já populado — seed ignorado.");
            return;
        }

        log.info("DataInitializer: criando dados de demonstração...");

        criarPerfisEUsuarios();
        criarFuncionarios();

        log.info("DataInitializer: dados de demonstração criados com sucesso.");
    }

    // -------------------------------------------------------------------------
    // Perfis e usuários
    // -------------------------------------------------------------------------

    private void criarPerfisEUsuarios() {
        Perfil admin      = perfil("ADMIN",       "Acesso total ao sistema");
        Perfil gestor     = perfil("GESTOR",      "Gerencia equipes e aprova registros");
        Perfil rh         = perfil("RH",          "Gestão de pessoas e relatórios");
        Perfil funcionario = perfil("FUNCIONARIO", "Registra ponto e consulta próprio saldo");

        usuario("Super Admin",    "admin@bancohoras.com",        "admin123",   admin,      StatusUsuario.ATIVO);
        usuario("Carlos Mendes",  "carlos.mendes@empresa.com",   "gestor123",  gestor,     StatusUsuario.ATIVO);
        usuario("Paula Reis",     "paula.rh@empresa.com",        "rh123",      rh,         StatusUsuario.ATIVO);
        usuario("João Ferreira",  "joao.f@empresa.com",          "joao123",    funcionario, StatusUsuario.ATIVO);

        log.info("DataInitializer: {} perfis e {} usuários criados.",
            perfilRepository.count(), usuarioRepository.count());
    }

    // -------------------------------------------------------------------------
    // Funcionários e banco de horas
    // -------------------------------------------------------------------------

    private void criarFuncionarios() {
        //                    nome              cargo                  email                     jornada  entrada   saída   intervalo  saldo (min)
        funcionario("Rafael Souza", "Desenvolvedor Sênior", "rafael.souza@empresa.com",  8,  "09:00", "18:00", "1h",   2880);
        funcionario("Ana Lima",     "Analista de Sistemas",  "ana.lima@empresa.com",      8,  "08:00", "17:00", "1h",   1320);
        funcionario("Pedro Alves",  "Desenvolvedor Pleno",   "pedro.alves@empresa.com",   8,  "09:00", "18:00", "1h",   1920);
        funcionario("Carla Nunes",  "Scrum Master",          "carla.nunes@empresa.com",   8,  "08:00", "17:00", "1h",   1080);
        funcionario("Tatiane Melo", "UX Designer",           "tatiane.melo@empresa.com",  8,  "08:30", "17:30", "1h",      0);

        log.info("DataInitializer: {} funcionários criados.", funcionarioRepository.count());
    }

    // -------------------------------------------------------------------------
    // Helpers de criação
    // -------------------------------------------------------------------------

    private Perfil perfil(String nome, String descricao) {
        return perfilRepository.findByNome(nome).orElseGet(() ->
            perfilRepository.save(
                Perfil.builder()
                    .nome(nome)
                    .descricao(descricao)
                    .build()
            )
        );
    }

    private Usuario usuario(String nome, String email, String senhaPura, Perfil perfil, StatusUsuario status) {
        return usuarioRepository.save(
            Usuario.builder()
                .nome(nome)
                .email(email.toLowerCase())
                .senhaHash(passwordEncoder.encode(senhaPura))
                .perfil(perfil)
                .status(status)
                .ativo(true)
                .build()
        );
    }

    private void funcionario(
        String nome, String cargo, String email,
        int jornadaDiaria, String horarioEntrada, String horarioSaida,
        String intervalo, int saldoMinutos
    ) {
        Funcionario func = funcionarioRepository.save(
            Funcionario.builder()
                .nome(nome)
                .cargo(cargo)
                .email(email)
                .jornadaDiaria(jornadaDiaria)
                .horarioEntrada(horarioEntrada)
                .horarioSaida(horarioSaida)
                .intervalo(intervalo)
                .ativo(true)
                .build()
        );

        bancoHorasRepository.save(
            BancoHoras.builder()
                .funcionario(func)
                .saldoAtualMinutos(saldoMinutos)
                .dataVencimento(LocalDate.now().plusYears(1))
                .build()
        );
    }
}
