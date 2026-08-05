package com.audit.dgi.validateur_dgi.security;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import com.audit.dgi.validateur_dgi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final SuperAdminService superAdminService;

    public AppUserDetailsService(UserRepository userRepository, SuperAdminService superAdminService) {
        this.userRepository = userRepository;
        this.superAdminService = superAdminService;
    }

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        user.setSuperAdmin(superAdminService.isSuperAdmin(username));
        return user;
    }
}
