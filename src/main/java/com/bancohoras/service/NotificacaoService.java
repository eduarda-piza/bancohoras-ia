package com.bancohoras.service;

import com.bancohoras.model.Notificacao;
import com.bancohoras.repository.NotificacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public List<Notificacao> listarNaoLidas(UUID usuarioId) {
        return notificacaoRepository.findByDestinatarioIdAndLidaFalse(usuarioId);
    }

    public List<Notificacao> listarTodas(UUID usuarioId) {
        return notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(usuarioId);
    }

    public long contarNaoLidas(UUID usuarioId) {
        return notificacaoRepository.countByDestinatarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLida(UUID notificacaoId) {
        Notificacao n = notificacaoRepository.findById(notificacaoId)
            .orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada: " + notificacaoId));
        n.setLida(true);
        notificacaoRepository.save(n);
    }

    @Transactional
    public void marcarTodasComoLidas(UUID usuarioId) {
        notificacaoRepository.findByDestinatarioIdAndLidaFalse(usuarioId)
            .forEach(n -> {
                n.setLida(true);
                notificacaoRepository.save(n);
            });
    }
}
