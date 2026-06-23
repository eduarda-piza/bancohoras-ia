package com.bancohoras.controller;

import com.bancohoras.model.BancoHoras;
import com.bancohoras.model.Notificacao;
import com.bancohoras.model.RegistroPonto;
import com.bancohoras.repository.BancoHorasRepository;
import com.bancohoras.repository.FuncionarioRepository;
import com.bancohoras.repository.NotificacaoRepository;
import com.bancohoras.repository.UsuarioRepository;
import com.bancohoras.service.RegistroPontoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final FuncionarioRepository funcionarioRepository;
    private final BancoHorasRepository  bancoHorasRepository;
    private final RegistroPontoService  registroPontoService;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository     usuarioRepository;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user, Model model) {
        List<BancoHoras>    equipe    = bancoHorasRepository.findAllWithFuncionario();
        List<RegistroPonto> pendentes = registroPontoService.listarPendentes();
        long totalAtivos   = funcionarioRepository.findAllByAtivoTrue().size();
        long totalCriticos = equipe.stream().filter(b -> b.getSaldoAtualMinutos() > 2400).count();

        long naoLidas = 0;
        List<Notificacao> alertasRecentes = List.of();

        var usuarioOpt = usuarioRepository.findByEmail(user.getUsername());
        if (usuarioOpt.isPresent()) {
            UUID uid = usuarioOpt.get().getId();
            naoLidas        = notificacaoRepository.countByDestinatarioIdAndLidaFalse(uid);
            alertasRecentes = notificacaoRepository
                .findByDestinatarioIdOrderByDataCriacaoDesc(uid)
                .stream().limit(4).toList();
        }

        List<RegistroPonto> aprovacoesPendentes = pendentes.stream().limit(3).toList();

        model.addAttribute("totalAtivos",          totalAtivos);
        model.addAttribute("totalCriticos",        totalCriticos);
        model.addAttribute("totalPendentes",       pendentes.size());
        model.addAttribute("naoLidas",             naoLidas);
        model.addAttribute("equipe",               equipe);
        model.addAttribute("alertas",              alertasRecentes);
        model.addAttribute("alertasRecentes",      alertasRecentes);
        model.addAttribute("aprovacoesPendentes",  aprovacoesPendentes);
        model.addAttribute("pageTitle",            "Visão Geral");
        return "dashboard";
    }
}
