package com.bancohoras.service;

import com.bancohoras.model.Funcionario;
import com.bancohoras.model.RegistroPonto;
import com.bancohoras.model.enums.StatusRegistro;
import com.bancohoras.repository.FuncionarioRepository;
import com.bancohoras.repository.RegistroPontoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistroPontoService {

    private final RegistroPontoRepository registroPontoRepository;
    private final FuncionarioRepository   funcionarioRepository;
    private final BancoHorasService       bancoHorasService;

    // -------------------------------------------------------------------------
    // Registro de ponto
    // -------------------------------------------------------------------------

    @Transactional
    public RegistroPonto registrarEntrada(UUID funcionarioId) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
            .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + funcionarioId));

        return registroPontoRepository.save(
            RegistroPonto.builder()
                .funcionario(funcionario)
                .entrada(LocalDateTime.now())
                .status(StatusRegistro.PENDENTE)
                .build()
        );
    }

    @Transactional
    public RegistroPonto registrarSaida(UUID funcionarioId) {
        RegistroPonto registro = registroPontoRepository
            .findFirstByFuncionarioIdAndSaidaIsNullOrderByEntradaDesc(funcionarioId)
            .orElseThrow(() -> new IllegalStateException(
                "Nenhum registro de entrada em aberto para: " + funcionarioId));

        LocalDateTime saida = LocalDateTime.now();
        registro.setSaida(saida);
        registro.setDuracaoMinutos((int) Duration.between(registro.getEntrada(), saida).toMinutes());
        return registroPontoRepository.save(registro);
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    public List<RegistroPonto> listarPorFuncionario(UUID funcionarioId) {
        return registroPontoRepository.findByFuncionarioIdFetchAll(funcionarioId);
    }

    public List<RegistroPonto> listarPendentes() {
        return registroPontoRepository.findByStatusFetchFuncionario(StatusRegistro.PENDENTE);
    }

    // -------------------------------------------------------------------------
    // Aprovação / rejeição
    // -------------------------------------------------------------------------

    @Transactional
    public RegistroPonto aprovar(UUID registroId) {
        RegistroPonto registro = registroPontoRepository.findById(registroId)
            .orElseThrow(() -> new EntityNotFoundException("Registro não encontrado: " + registroId));

        registro.setStatus(StatusRegistro.APROVADO);
        RegistroPonto salvo = registroPontoRepository.save(registro);
        bancoHorasService.atualizarSaldo(registro.getFuncionario().getId());
        return salvo;
    }

    @Transactional
    public RegistroPonto rejeitar(UUID registroId) {
        RegistroPonto registro = registroPontoRepository.findById(registroId)
            .orElseThrow(() -> new EntityNotFoundException("Registro não encontrado: " + registroId));

        registro.setStatus(StatusRegistro.REJEITADO);
        return registroPontoRepository.save(registro);
    }
}
