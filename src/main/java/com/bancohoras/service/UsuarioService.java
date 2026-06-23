package com.bancohoras.service;

import com.bancohoras.model.Usuario;
import com.bancohoras.model.enums.StatusUsuario;
import com.bancohoras.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Regras de negócio para usuários do sistema.
 * A lógica de autenticação Spring Security está em CustomUserDetailsService.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @Transactional
    public Usuario salvar(Usuario usuario) {
        usuario.setSenhaHash(passwordEncoder.encode(usuario.getSenhaHash()));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // -------------------------------------------------------------------------
    // Ativação / desativação
    // -------------------------------------------------------------------------

    @Transactional
    public void ativar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
        usuario.setStatus(StatusUsuario.ATIVO);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}
