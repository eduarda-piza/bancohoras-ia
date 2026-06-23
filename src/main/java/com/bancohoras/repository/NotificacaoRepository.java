package com.bancohoras.repository;

import com.bancohoras.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    /** Notificações não lidas de um usuário */
    List<Notificacao> findByDestinatarioIdAndLidaFalse(UUID usuarioId);

    /** Todas as notificações de um usuário, mais recente primeiro */
    List<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    /** Contagem de não lidas — útil para badge na interface */
    long countByDestinatarioIdAndLidaFalse(UUID usuarioId);
}
