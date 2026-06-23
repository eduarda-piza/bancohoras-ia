package com.bancohoras.repository;

import com.bancohoras.model.BancoHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BancoHorasRepository extends JpaRepository<BancoHoras, UUID> {

    Optional<BancoHoras> findByFuncionarioId(UUID funcionarioId);

    List<BancoHoras> findAllBySaldoAtualMinutosGreaterThan(int minutos);

    List<BancoHoras> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);

    /** JOIN FETCH — carrega funcionário na mesma query para evitar N+1 */
    @Query("SELECT b FROM BancoHoras b JOIN FETCH b.funcionario ORDER BY b.funcionario.nome")
    List<BancoHoras> findAllWithFuncionario();

    @Query("SELECT b FROM BancoHoras b JOIN FETCH b.funcionario WHERE b.saldoAtualMinutos > :minutos ORDER BY b.saldoAtualMinutos DESC")
    List<BancoHoras> findCriticosWithFuncionario(int minutos);
}
