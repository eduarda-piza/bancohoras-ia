package com.bancohoras.repository;

import com.bancohoras.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, UUID> {

    List<Funcionario> findAllByAtivoTrue();

    List<Funcionario> findByCargo(String cargo);

    boolean existsByEmail(String email);
}
