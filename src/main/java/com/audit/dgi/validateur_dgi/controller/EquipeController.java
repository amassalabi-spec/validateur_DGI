package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import com.audit.dgi.validateur_dgi.repository.UserRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/equipe")
public class EquipeController {

    public static final int SEAT_LIMIT = 10;

    private final UserRepository userRepository;
    private final SessionUserService sessionUserService;
    private final PasswordEncoder passwordEncoder;

    public EquipeController(UserRepository userRepository,
                            SessionUserService sessionUserService,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionUserService = sessionUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String equipe(Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        List<AppUser> membres = userRepository.findByCompanyIdOrderByIdAsc(companyId);
        model.addAttribute("membres", membres);
        model.addAttribute("seatsUsed", membres.size());
        model.addAttribute("seatLimit", SEAT_LIMIT);
        return "equipe";
    }

    @PostMapping("/invite")
    public String invite(@RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam(defaultValue = "ROLE_USER") String role,
                         @RequestParam String password,
                         RedirectAttributes redirectAttributes) {
        Long companyId = sessionUserService.currentCompanyId();
        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Un compte existe déjà avec cet email.");
            return "redirect:/equipe";
        }
        AppUser user = AppUser.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .companyId(companyId)
                .active(true)
                .build();
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("saved", true);
        return "redirect:/equipe";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long companyId = sessionUserService.currentCompanyId();
        AppUser user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("User not found");
        }
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("saved", true);
        return "redirect:/equipe";
    }
}
