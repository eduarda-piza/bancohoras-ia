package com.bancohoras.controller;

import com.bancohoras.service.RegistroPontoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/aprovacoes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GESTOR','RH')")
public class AprovacaoController {

    private final RegistroPontoService registroPontoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pendentes",  registroPontoService.listarPendentes());
        model.addAttribute("pageTitle",  "Aprovações");
        return "aprovacoes";
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            registroPontoService.aprovar(id);
            ra.addFlashAttribute("sucesso", "Registro aprovado e banco de horas atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao aprovar: " + e.getMessage());
        }
        return "redirect:/aprovacoes";
    }

    @PostMapping("/{id}/rejeitar")
    public String rejeitar(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            registroPontoService.rejeitar(id);
            ra.addFlashAttribute("sucesso", "Registro rejeitado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao rejeitar: " + e.getMessage());
        }
        return "redirect:/aprovacoes";
    }

    @PostMapping("/lote")
    public String aprovarLote(@RequestParam(required = false) List<UUID> ids, RedirectAttributes ra) {
        if (ids == null || ids.isEmpty()) {
            ra.addFlashAttribute("aviso", "Nenhum registro selecionado.");
            return "redirect:/aprovacoes";
        }
        int sucesso = 0;
        for (UUID id : ids) {
            try {
                registroPontoService.aprovar(id);
                sucesso++;
            } catch (Exception ignored) {}
        }
        ra.addFlashAttribute("sucesso", sucesso + " registro(s) aprovado(s).");
        return "redirect:/aprovacoes";
    }
}
