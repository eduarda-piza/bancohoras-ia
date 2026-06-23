package com.bancohoras.config;

import com.bancohoras.model.Usuario;
import com.bancohoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementação do UserDetailsService do Spring Security.
 * Mantida no pacote config para separar a lógica de autenticação
 * da lógica de negócio de UsuarioService.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuário não encontrado para o e-mail: " + email));

        // Verifica conta antes de montar o UserDetails
        if (!usuario.isAtivo()) {
            throw new DisabledException("A conta do usuário está desativada: " + email);
        }

        String role = (usuario.getPerfil() != null)
            ? "ROLE_" + usuario.getPerfil().getNome()   // ex: ROLE_ADMIN, ROLE_GESTOR
            : "ROLE_FUNCIONARIO";

        return new org.springframework.security.core.userdetails.User(
            usuario.getEmail(),
            usuario.getSenhaHash(),
            /* enabled             */ true,
            /* accountNonExpired   */ true,
            /* credentialsNonExpired */ true,
            /* accountNonLocked    */ true,
            List.of(new SimpleGrantedAuthority(role))
        );
    }
}
