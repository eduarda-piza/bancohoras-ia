package com.bancohoras.repository;

import com.bancohoras.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Usado pelo SistemaIAService para encontrar gestores e admins destinatários */
    List<Usuario> findByPerfil_NomeIn(List<String> nomes);

    List<Usuario> findByAtivoTrue();
}
