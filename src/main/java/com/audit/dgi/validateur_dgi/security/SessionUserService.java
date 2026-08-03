package com.audit.dgi.validateur_dgi.security;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionUserService {

    public AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Aucune session authentifiée");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUser appUser) {
            return appUser;
        }
        throw new IllegalStateException("Principal de session invalide: " + principal.getClass().getName());
    }

    public Long currentCompanyId() {
        return currentUser().getCompanyId();
    }
}

