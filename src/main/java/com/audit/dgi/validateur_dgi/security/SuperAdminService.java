package com.audit.dgi.validateur_dgi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Désigne les utilisateurs "Super Admin" de la plateforme.
 * La liste des emails est configurée dans application.properties
 * (app.super.admin.emails, séparateur virgule). Ces comptes obtiennent
 * l'autorité ROLE_SUPER_ADMIN en plus de leur rôle d'entreprise.
 */
@Service
public class SuperAdminService {

    private final Set<String> emails;

    public SuperAdminService(@Value("${app.super.admin.emails:}") String emailsCsv) {
        this.emails = emailsCsv == null || emailsCsv.isBlank()
                ? Set.of()
                : Arrays.stream(emailsCsv.split(","))
                        .map(String::trim)
                        .map(s -> s.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet());
    }

    public boolean isSuperAdmin(String email) {
        return email != null && emails.contains(email.toLowerCase(Locale.ROOT));
    }
}
