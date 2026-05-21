package com.example.pfebtk.auth.repository;

import com.example.pfebtk.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUnix(String unix);
    boolean existsByEmail(String email);
    List<User> findByPuti(String puti);

}
