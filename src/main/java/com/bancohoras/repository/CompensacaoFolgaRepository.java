package com.bancohoras.repository;

import com.bancohoras.model.CompensacaoFolga;
import com.bancohoras.model.enums.StatusCompensacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompensacaoFolgaRepository extends JpaRepository<CompensacaoFolga, UUID> {

    /** Histórico de compensações de um funcionário, mais recente primeiro */
    List<CompensacaoFolga> findByFuncionarioIdOrderByDataSolicitacaoDesc(UUID funcionarioId);

    /** Filtra compensações por status — ex: todas SOLICITADAS aguardando aprovação */
    List<CompensacaoFolga> findAllByStatus(StatusCompensacao status);

    /** Combinação status + funcionário — ex: folgas APROVADAS de um funcionário */
    List<CompensacaoFolga> findByFuncionarioIdAndStatus(UUID funcionarioId, StatusCompensacao status);
}
