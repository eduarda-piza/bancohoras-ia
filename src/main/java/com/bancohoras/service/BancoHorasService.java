package com.bancohoras.service;

import com.bancohoras.model.BancoHoras;
import com.bancohoras.model.Funcionario;
import com.bancohoras.model.RegistroPonto;
import com.bancohoras.model.enums.StatusRegistro;
import com.bancohoras.repository.BancoHorasRepository;
import com.bancohoras.repository.FuncionarioRepository;
import com.bancohoras.repository.RegistroPontoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BancoHorasService {

    private final BancoHorasRepository bancoHorasRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final RegistroPontoRepository registroPontoRepository;

    // -------------------------------------------------------------------------
    // Saldo
    // -------------------------------------------------------------------------

    /**
     * Recalcula o saldo somando (duração real − jornada diária) de cada registro
     * APROVADO com saída preenchida. Saldo positivo = horas extras; negativo = débito.
     */
    @Transactional
    public BancoHoras atualizarSaldo(UUID funcionarioId) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
            .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + funcionarioId));

        BancoHoras banco = bancoHorasRepository.findByFuncionarioId(funcionarioId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Banco de horas não encontrado para o funcionário: " + funcionarioId));

        int jornadaMinutos = funcionario.getJornadaDiaria() * 60;

        List<RegistroPonto> aprovados = registroPontoRepository
            .findByStatusAndFuncionarioId(StatusRegistro.APROVADO, funcionarioId);

        int saldo = aprovados.stream()
            .filter(r -> r.getSaida() != null)
            .mapToInt(r -> r.getDuracaoMinutos() - jornadaMinutos)
            .sum();

        banco.setSaldoAtualMinutos(saldo);
        return bancoHorasRepository.save(banco);
    }

    public Optional<BancoHoras> buscarPorFuncionario(UUID funcionarioId) {
        return bancoHorasRepository.findByFuncionarioId(funcionarioId);
    }

    // -------------------------------------------------------------------------
    // Vencimento (CLT: prazo de 1 ano a partir do primeiro registro)
    // -------------------------------------------------------------------------

    /**
     * Retorna a data-limite para compensação das horas, calculada como
     * 1 ano após o primeiro registro de ponto do funcionário.
     * Se não houver registro, considera 1 ano a partir de hoje.
     */
    public LocalDate calcularVencimento(UUID funcionarioId) {
        return registroPontoRepository
            .findDataPrimeiroRegistro(funcionarioId)
            .map(dt -> dt.toLocalDate().plusYears(1))
            .orElse(LocalDate.now().plusYears(1));
    }

    // -------------------------------------------------------------------------
    // Alertas
    // -------------------------------------------------------------------------

    /**
     * Retorna funcionários cujo saldo ultrapassa 40 horas (2.400 minutos),
     * situação que exige atenção do RH/gestor para evitar passivo trabalhista.
     */
    public List<Funcionario> listarCriticos() {
        return bancoHorasRepository.findAllBySaldoAtualMinutosGreaterThan(2400)
            .stream()
            .map(BancoHoras::getFuncionario)
            .collect(Collectors.toList());
    }

    /**
     * Atualiza a data de vencimento persistida no BancoHoras com base no
     * cálculo CLT, mantendo o registro sempre correto no banco de dados.
     */
    @Transactional
    public BancoHoras sincronizarVencimento(UUID funcionarioId) {
        BancoHoras banco = bancoHorasRepository.findByFuncionarioId(funcionarioId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Banco de horas não encontrado para o funcionário: " + funcionarioId));

        banco.setDataVencimento(calcularVencimento(funcionarioId));
        return bancoHorasRepository.save(banco);
    }
}
