package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("pageTitle", "Profile");
        model.addAttribute("username", "trader");
        model.addAttribute("email", "trader@example.com");
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String email, Model model) {
        // TODO: Implement profile update logic
        // 1. Validate email format
        // 2. Check if email is already in use
        // 3. Update user email in database
        model.addAttribute("pageTitle", "Profile");
        model.addAttribute("username", "trader");
        model.addAttribute("email", email);
        model.addAttribute("message", "Profile updated successfully");
        return "profile";
    }
}
