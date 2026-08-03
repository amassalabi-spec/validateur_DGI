package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.repository.UserRepository;
import com.audit.dgi.validateur_dgi.service.auth.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public RegisterController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(required = false) String companyName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Un compte existe déjà avec cet email. Connectez-vous.");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas.");
            return "register";
        }
        String name = companyName == null || companyName.isBlank() ? "Company for " + email : companyName.trim();
        authService.register(email, password, name);
        return "redirect:/login?registered";
    }
}
