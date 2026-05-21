package com.example.pfebtk.auth.repository;

import com.example.pfebtk.auth.entity.PasswordHistory;
import com.example.pfebtk.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordHistoryRepo extends JpaRepository<PasswordHistory, Long> {
    // dernier ADMIN record pas encore changé
    Optional<PasswordHistory> findTopByUserOrderByCreatedAtDesc(User user);
}
