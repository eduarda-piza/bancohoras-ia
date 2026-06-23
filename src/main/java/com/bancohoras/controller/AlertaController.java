package com.bancohoras.controller;

import com.bancohoras.repository.UsuarioRepository;
import com.bancohoras.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final NotificacaoService  notificacaoService;
    private final UsuarioRepository   usuarioRepository;

    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails user, Model model) {
        usuarioRepository.findByEmail(user.getUsername()).ifPresent(u -> {
            model.addAttribute("notificacoes", notificacaoService.listarTodas(u.getId()));
        });
        model.addAttribute("pageTitle", "Alertas");
        return "alertas";
    }

    @PostMapping("/{id}/marcar-lida")
    public String marcarLida(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            notificacaoService.marcarComoLida(id);
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/alertas";
    }

    @PostMapping("/marcar-todas-lidas")
    public String marcarTodasLidas(@AuthenticationPrincipal UserDetails user, RedirectAttributes ra) {
        usuarioRepository.findByEmail(user.getUsername()).ifPresent(u ->
            notificacaoService.marcarTodasComoLidas(u.getId())
        );
        ra.addFlashAttribute("sucesso", "Todas as notificações foram marcadas como lidas.");
        return "redirect:/alertas";
    }
}
