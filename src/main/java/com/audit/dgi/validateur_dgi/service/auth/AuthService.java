package com.audit.dgi.validateur_dgi.service.auth;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(String email, String rawPassword, String companyName) {
        Company company = Company.builder().name(companyName).build();
        company = companyRepository.save(company);

        AppUser user = AppUser.builder()
                .email(email)
                .fullName(companyName)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role("ROLE_ADMIN")
                .companyId(company.getId())
                .active(true)
                .build();
        return userRepository.save(user);
    }
}
