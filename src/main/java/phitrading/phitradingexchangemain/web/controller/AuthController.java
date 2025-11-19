package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import phitrading.phitradingexchangemain.web.dto.RegisterRequest;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final com.phitrading.exchange.domain.service.RegistrationService registrationService;

    public AuthController(com.phitrading.exchange.domain.service.RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "auth-login";
    }

    // Note: POST /auth/login is handled by Spring Security formLogin. Do not implement here.

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Register");
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "auth-register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute RegisterRequest registerRequest, Model model) {
        // Validate password match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("registerRequest", registerRequest);
            return "auth-register";
        }

        try {
            registrationService.registerNewUser(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());
            return "redirect:/auth/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            return "auth-register";
        }
    }
}
