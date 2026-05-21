package com.example.pfebtk.annonce.repository;

import com.example.pfebtk.annonce.entity.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConventionRepo extends JpaRepository<Convention, Long> {
}
