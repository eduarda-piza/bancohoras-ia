package com.bancohoras.repository;

import com.bancohoras.model.RegistroPonto;
import com.bancohoras.model.enums.StatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, UUID> {

    List<RegistroPonto> findByFuncionarioIdOrderByEntradaDesc(UUID funcionarioId);

    List<RegistroPonto> findByStatusAndFuncionarioId(StatusRegistro status, UUID funcionarioId);

    Optional<RegistroPonto> findFirstByFuncionarioIdAndSaidaIsNullOrderByEntradaDesc(UUID funcionarioId);

    /** JOIN FETCH para evitar LazyInitializationException ao renderizar templates */
    @Query("SELECT r FROM RegistroPonto r JOIN FETCH r.funcionario WHERE r.status = :status ORDER BY r.dataCriacao DESC")
    List<RegistroPonto> findByStatusFetchFuncionario(@Param("status") StatusRegistro status);

    /** JOIN FETCH para o histórico individual do funcionário */
    @Query("SELECT r FROM RegistroPonto r JOIN FETCH r.funcionario WHERE r.funcionario.id = :id ORDER BY r.entrada DESC")
    List<RegistroPonto> findByFuncionarioIdFetchAll(@Param("id") UUID funcionarioId);

    @Query("SELECT r FROM RegistroPonto r JOIN FETCH r.funcionario WHERE r.funcionario.id = :funcionarioId AND r.saida IS NULL AND r.entrada >= :desde AND r.sugeridoPelaIA = false")
    List<RegistroPonto> findRegistrosAbertosDesde(@Param("funcionarioId") UUID funcionarioId, @Param("desde") LocalDateTime desde);

    @Query("SELECT r FROM RegistroPonto r JOIN FETCH r.funcionario WHERE r.saida IS NOT NULL AND r.duracaoMinutos > :limiteMinutos AND r.entrada >= :inicioDia")
    List<RegistroPonto> findExcessosJornada(@Param("limiteMinutos") int limiteMinutos, @Param("inicioDia") LocalDateTime inicioDia);

    @Query("SELECT MIN(r.entrada) FROM RegistroPonto r WHERE r.funcionario.id = :funcionarioId")
    Optional<LocalDateTime> findDataPrimeiroRegistro(@Param("funcionarioId") UUID funcionarioId);

    @Query("SELECT SUM(r.duracaoMinutos) FROM RegistroPonto r WHERE r.funcionario.id = :funcionarioId AND r.status = 'APROVADO' AND r.saida IS NOT NULL")
    Optional<Integer> somarMinutosAprovados(@Param("funcionarioId") UUID funcionarioId);
}
