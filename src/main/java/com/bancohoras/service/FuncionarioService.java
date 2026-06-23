package com.bancohoras.service;

import com.bancohoras.model.BancoHoras;
import com.bancohoras.model.Funcionario;
import com.bancohoras.repository.BancoHorasRepository;
import com.bancohoras.repository.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final BancoHorasRepository bancoHorasRepository;

    /**
     * Persiste o funcionário e abre um BancoHoras zerado para ele.
     * Ambas as operações ocorrem na mesma transação.
     */
    @Transactional
    public Funcionario salvar(Funcionario funcionario) {
        Funcionario salvo = funcionarioRepository.save(funcionario);

        BancoHoras banco = BancoHoras.builder()
            .funcionario(salvo)
            .saldoAtualMinutos(0)
            .ultimaAtualizacao(LocalDateTime.now())
            .build();
        bancoHorasRepository.save(banco);

        return salvo;
    }

    @Transactional
    public Funcionario atualizar(Funcionario funcionario) {
        if (!funcionarioRepository.existsById(funcionario.getId())) {
            throw new EntityNotFoundException("Funcionário não encontrado: " + funcionario.getId());
        }
        return funcionarioRepository.save(funcionario);
    }

    public List<Funcionario> listarAtivos() {
        return funcionarioRepository.findAllByAtivoTrue();
    }

    public Optional<Funcionario> buscarPorId(UUID id) {
        return funcionarioRepository.findById(id);
    }

    @Transactional
    public void desativar(UUID id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + id));
        funcionario.setAtivo(false);
        funcionarioRepository.save(funcionario);
    }
}
