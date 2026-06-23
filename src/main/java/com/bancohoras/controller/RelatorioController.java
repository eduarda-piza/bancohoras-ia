package com.bancohoras.controller;

import com.bancohoras.model.BancoHoras;
import com.bancohoras.repository.BancoHorasRepository;
import com.bancohoras.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GESTOR','RH')")
public class RelatorioController {

    private final BancoHorasRepository  bancoHorasRepository;
    private final FuncionarioRepository funcionarioRepository;

    @GetMapping
    public String index(Model model) {
        List<BancoHoras> equipe = bancoHorasRepository.findAllWithFuncionario();

        long totalCriticos  = equipe.stream().filter(b -> b.getSaldoAtualMinutos() > 2400).count();
        long totalOk        = equipe.stream().filter(b -> b.getSaldoAtualMinutos() <= 1200).count();
        int  totalSaldoMins = equipe.stream().mapToInt(BancoHoras::getSaldoAtualMinutos).sum();

        model.addAttribute("equipe",          equipe);
        model.addAttribute("totalCriticos",   totalCriticos);
        model.addAttribute("totalOk",         totalOk);
        model.addAttribute("totalSaldoHoras", totalSaldoMins / 60);
        model.addAttribute("totalHorasEquipe", totalSaldoMins / 60);
        model.addAttribute("agora",           java.time.LocalDateTime.now());
        model.addAttribute("pageTitle",       "Relatórios");
        return "relatorios";
    }

    @GetMapping("/pdf")
    public String pdf(Model model) {
        List<BancoHoras> equipe = bancoHorasRepository.findAllWithFuncionario();
        long totalCriticos  = equipe.stream().filter(b -> b.getSaldoAtualMinutos() > 2400).count();
        long totalOk        = equipe.stream().filter(b -> b.getSaldoAtualMinutos() <= 1200).count();
        int  totalSaldoMins = equipe.stream().mapToInt(BancoHoras::getSaldoAtualMinutos).sum();

        model.addAttribute("equipe",         equipe);
        model.addAttribute("totalCriticos",  totalCriticos);
        model.addAttribute("totalOk",        totalOk);
        model.addAttribute("totalSaldoHoras", totalSaldoMins / 60);
        model.addAttribute("agora",          java.time.LocalDateTime.now());
        return "relatorios-print";
    }
}
