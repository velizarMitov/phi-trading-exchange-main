package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.ProfileService;
import com.phitrading.exchange.web.dto.UpdateProfileRequest;
import com.phitrading.exchange.web.dto.UserProfileDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        String username = principal != null ? principal.getName() : null;
        UserProfileDto profile = profileService.getProfile(username);
        model.addAttribute("pageTitle", "Profile");
        model.addAttribute("profile", profile);
        // Pre-populate the update form with the current email so th:field shows the value
        UpdateProfileRequest update = new UpdateProfileRequest();
        update.setEmail(profile.getEmail());
        model.addAttribute("updateRequest", update);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Principal principal,
                                @Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        String username = principal != null ? principal.getName() : null;
        if (bindingResult.hasErrors()) {
            // re-render with validation errors
            UserProfileDto profile = profileService.getProfile(username);
            model.addAttribute("pageTitle", "Profile");
            model.addAttribute("profile", profile);
            return "profile";
        }

        try {
            profileService.updateProfile(username, request);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
