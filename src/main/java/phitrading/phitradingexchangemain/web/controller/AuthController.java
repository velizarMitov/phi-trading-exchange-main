package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import phitrading.phitradingexchangemain.web.dto.RegisterRequest;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "auth-login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username, @RequestParam String password, Model model) {
        // TODO: Implement user authentication with Spring Security
        // For now, redirect to dashboard on any submission
        model.addAttribute("username", username);
        return "redirect:/dashboard";
    }

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
        // TODO: Implement user registration logic
        // Validate password match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("registerRequest", registerRequest);
            return "auth-register";
        }

        // TODO: Save user to database
        // For now, redirect to login
        return "redirect:/auth/login";
    }
}
