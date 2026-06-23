package com.bancohoras.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Rotas de autenticação.
 * O POST /login e GET /logout são interceptados e processados
 * automaticamente pelo Spring Security — não precisam de métodos aqui.
 */
@Controller
public class AuthController {

    /**
     * Exibe o formulário de login.
     *
     * @param error   presente na URL quando o Spring Security falha na autenticação
     * @param logout  presente na URL após logout bem-sucedido
     * @param model   modelo Thymeleaf — recebe mensagens de feedback
     */
    @GetMapping("/login")
    public String login(
        @RequestParam(value = "error",  required = false) String error,
        @RequestParam(value = "logout", required = false) String logout,
        Model model
    ) {
        if (error != null) {
            model.addAttribute("mensagemErro",
                "E-mail ou senha incorretos. Verifique suas credenciais e tente novamente.");
        }

        if (logout != null) {
            model.addAttribute("mensagemSucesso",
                "Você saiu com segurança. Até logo!");
        }

        return "login";
    }
}
