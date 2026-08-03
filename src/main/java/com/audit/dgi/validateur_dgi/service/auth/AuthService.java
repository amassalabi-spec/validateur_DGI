package com.audit.dgi.validateur_dgi.service.auth;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.dto.auth.AuthRequest;
import com.audit.dgi.validateur_dgi.dto.auth.AuthResponse;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.repository.UserRepository;
import com.audit.dgi.validateur_dgi.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AppUser register(String email, String rawPassword, String companyName) {
        Company company = Company.builder().name(companyName).build();
        company = companyRepository.save(company);

        AppUser user = AppUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role("ROLE_ADMIN")
                .companyId(company.getId())
                .build();
        return userRepository.save(user);
    }

    public String login(AuthRequest request) {
        var userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) throw new IllegalArgumentException("Invalid credentials");
        AppUser user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        return tokenProvider.createToken(user.getEmail(), user.getRole(), user.getCompanyId());
    }

    /**
     * Register et retourner la réponse complète avec token et données utilisateur
     */
    public AuthResponse registerAndGetResponse(String email, String rawPassword, String companyName) {
        Company company = Company.builder().name(companyName).build();
        company = companyRepository.save(company);

        AppUser user = AppUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role("ROLE_ADMIN")
                .companyId(company.getId())
                .build();
        user = userRepository.save(user);

        String token = tokenProvider.createToken(user.getEmail(), user.getRole(), user.getCompanyId());

        return AuthResponse.builder()
                .token(token)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .companyId(user.getCompanyId())
                        .companyName(company.getName())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * Login et retourner la réponse complète avec token et données utilisateur
     */
    public AuthResponse loginAndGetResponse(String email, String password) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new IllegalArgumentException("Invalid credentials");

        AppUser user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        var company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        String token = tokenProvider.createToken(user.getEmail(), user.getRole(), user.getCompanyId());

        return AuthResponse.builder()
                .token(token)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .companyId(user.getCompanyId())
                        .companyName(company.getName())
                        .role(user.getRole())
                        .build())
                .build();
    }
}

