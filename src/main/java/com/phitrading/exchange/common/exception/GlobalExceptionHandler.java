package com.phitrading.exchange.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.exceptions.TemplateInputException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateInputException.class)
    public String handleTemplateInputException(TemplateInputException ex, Model model, HttpServletRequest request) {
        log.error("Template parsing error at URI {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("title", "Something went wrong rendering this page");
        model.addAttribute("message", "We encountered a template error while rendering the page. Please try again in a moment or return to the home page.");
        return "error/generic-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        log.error("Unhandled exception at URI {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("title", "Unexpected error");
        model.addAttribute("message", "An unexpected error occurred. Our team has been notified. You can try again or go back to the home page.");
        return "error/generic-error";
    }

    @ExceptionHandler(SymbolInUseException.class)
    public String handleSymbolInUse(SymbolInUseException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("SymbolInUseException at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Cannot remove this symbol because it is already used in portfolio positions or orders.");
        return "redirect:/admin/symbols";
    }
}
