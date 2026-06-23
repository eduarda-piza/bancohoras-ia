package com.bancohoras.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Tratamento centralizado de exceções para controllers MVC.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Recurso não encontrado → página 404 simples. */
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(EntityNotFoundException e) {
        log.warn("Recurso não encontrado: {}", e.getMessage());
        return "error/404";
    }

    /**
     * Acesso negado por @PreAuthorize → redireciona ao dashboard com mensagem.
     * Cobre apenas violações a nível de método; violações de URL são tratadas
     * pela SecurityFilterChain antes de chegar aqui.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, RedirectAttributes ra) {
        log.warn("Acesso negado: {}", e.getMessage());
        ra.addFlashAttribute("erro", "Sem permissão de acesso.");
        return "redirect:/dashboard";
    }

    /** Qualquer outra exceção inesperada → página 500 simples. */
    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e) {
        log.error("Erro interno não tratado: {}", e.getMessage(), e);
        return "error/500";
    }
}
