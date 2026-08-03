package com.audit.dgi.validateur_dgi.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (tokenProvider.validateToken(token)) {
                Claims claims = tokenProvider.getClaims(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                Long companyId = claims.get("companyId", Long.class);

                java.util.List<SimpleGrantedAuthority> authorities = role == null ? java.util.List.of() : java.util.List.of(new SimpleGrantedAuthority(role));
                var auth = new UsernamePasswordAuthenticationToken(email, token, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                TenantContext.setCurrentTenant(companyId);
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            // clear tenant after request
            TenantContext.clear();
        }
    }
}

