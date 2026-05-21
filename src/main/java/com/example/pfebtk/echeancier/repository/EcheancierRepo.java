package com.example.pfebtk.echeancier.repository;

import com.example.pfebtk.echeancier.entity.Echeancier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EcheancierRepo extends JpaRepository<Echeancier, Long> {


    List<Echeancier> findByDemande_IdDemande(Long id);

    List<Echeancier> findByPayeFalseAndDateEcheanceBefore(LocalDate date);
}
