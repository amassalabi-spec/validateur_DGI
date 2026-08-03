package com.audit.dgi.validateur_dgi.repository;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}

