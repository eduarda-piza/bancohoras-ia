package com.bancohoras.controller;

import com.bancohoras.model.BancoHoras;
import com.bancohoras.model.Funcionario;
import com.bancohoras.model.RegistroPonto;
import com.bancohoras.repository.BancoHorasRepository;
import com.bancohoras.service.FuncionarioService;
import com.bancohoras.service.RegistroPontoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService   funcionarioService;
    private final RegistroPontoService registroPontoService;
    private final BancoHorasRepository bancoHorasRepository;

    @GetMapping
    public String listar(Model model) {
        List<BancoHoras> equipe = bancoHorasRepository.findAllWithFuncionario();
        model.addAttribute("equipe",     equipe);
        model.addAttribute("pageTitle",  "Funcionários");
        return "funcionarios";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("funcionario", Funcionario.builder()
            .jornadaDiaria(8).horarioEntrada("09:00").horarioSaida("18:00").intervalo("1h").build());
        model.addAttribute("pageTitle", "Novo Funcionário");
        return "funcionarios/novo";
    }

    @PostMapping("/novo")
    public String salvarNovo(@RequestParam String nome,
                              @RequestParam String cargo,
                              @RequestParam(required = false) String email,
                              @RequestParam(defaultValue = "8") int jornadaDiaria,
                              @RequestParam(defaultValue = "09:00") String horarioEntrada,
                              @RequestParam(defaultValue = "18:00") String horarioSaida,
                              @RequestParam(defaultValue = "1h") String intervalo,
                              RedirectAttributes ra) {
        try {
            Funcionario f = Funcionario.builder()
                .nome(nome.trim()).cargo(cargo.trim())
                .email(email != null && !email.isBlank() ? email.trim().toLowerCase() : null)
                .jornadaDiaria(jornadaDiaria)
                .horarioEntrada(horarioEntrada).horarioSaida(horarioSaida)
                .intervalo(intervalo).ativo(true)
                .build();
            Funcionario salvo = funcionarioService.salvar(f);
            ra.addFlashAttribute("sucesso", "Funcionário " + salvo.getNome() + " cadastrado com sucesso.");
            return "redirect:/funcionarios/" + salvo.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
            return "redirect:/funcionarios/novo";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable UUID id, Model model) {
        Funcionario funcionario = funcionarioService.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado: " + id));

        List<RegistroPonto> registros = registroPontoService.listarPorFuncionario(id);
        BancoHoras banco = bancoHorasRepository.findByFuncionarioId(id).orElse(null);

        long totalPendentes = registros.stream()
            .filter(r -> r.getStatus().name().equals("PENDENTE"))
            .count();

        model.addAttribute("funcionario",   funcionario);
        model.addAttribute("registros",     registros);
        model.addAttribute("banco",         banco);
        model.addAttribute("totalPendentes", totalPendentes);
        model.addAttribute("pageTitle",     funcionario.getNome());
        return "funcionario";
    }

    @GetMapping("/{id}/ponto/entrada")
    public String registrarEntrada(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            registroPontoService.registrarEntrada(id);
            ra.addFlashAttribute("sucesso", "Entrada registrada com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao registrar entrada: " + e.getMessage());
        }
        return "redirect:/funcionarios/" + id;
    }

    @GetMapping("/{id}/ponto/saida")
    public String registrarSaida(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            registroPontoService.registrarSaida(id);
            ra.addFlashAttribute("sucesso", "Saída registrada com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao registrar saída: " + e.getMessage());
        }
        return "redirect:/funcionarios/" + id;
    }
}
