package com.bancohoras.controller;

import com.bancohoras.model.BancoHoras;
import com.bancohoras.model.Notificacao;
import com.bancohoras.model.RegistroPonto;
import com.bancohoras.repository.BancoHorasRepository;
import com.bancohoras.repository.NotificacaoRepository;
import com.bancohoras.repository.RegistroPontoRepository;
import com.bancohoras.repository.UsuarioRepository;
import com.bancohoras.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final NotificacaoService      notificacaoService;
    private final NotificacaoRepository   notificacaoRepository;
    private final UsuarioRepository       usuarioRepository;
    private final BancoHorasRepository    bancoHorasRepository;
    private final RegistroPontoRepository registroPontoRepository;

    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails user, Model model) {
        // Todas as notificações, independente do usuário logado
        List<Notificacao> notificacoes = notificacaoRepository.findAll();
        notificacoes.sort((a, b) -> {
            if (a.getDataCriacao() == null) return 1;
            if (b.getDataCriacao() == null) return -1;
            return b.getDataCriacao().compareTo(a.getDataCriacao());
        });
        model.addAttribute("notificacoes", notificacoes);

        // Horas a vencer: saldo > 1200 min (>20h)
        List<BancoHoras> horasVencer = bancoHorasRepository.findCriticosWithFuncionario(1200);
        Map<UUID, Long> diasRestantesMap = new HashMap<>();
        LocalDate hoje = LocalDate.now();
        for (BancoHoras b : horasVencer) {
            if (b.getDataVencimento() != null) {
                long dias = ChronoUnit.DAYS.between(hoje, b.getDataVencimento());
                diasRestantesMap.put(b.getId(), dias);
            }
        }

        // Excesso de jornada: duracaoMinutos > 480 (>8h), registros dos últimos 30 dias
        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        List<RegistroPonto> excessoJornada = registroPontoRepository
            .findExcessosJornada(480, trintaDiasAtras);

        model.addAttribute("horasVencer",      horasVencer);
        model.addAttribute("diasRestantesMap", diasRestantesMap);
        model.addAttribute("excessoJornada",   excessoJornada);
        model.addAttribute("excessos",         excessoJornada);
        model.addAttribute("pageTitle",        "Alertas");
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
