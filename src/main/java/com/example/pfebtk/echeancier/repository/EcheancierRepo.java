package com.example.pfebtk.echeancier.repository;

import com.example.pfebtk.echeancier.entity.Echeancier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EcheancierRepo extends JpaRepository<Echeancier, Long> {


    List<Echeancier> findByDemande_IdDemande(Long id);

    @Query("""
        SELECT e
        FROM Echeancier e
        ORDER BY
            CASE
                WHEN e.statut = com.example.pfebtk.echeancier.entity.StatutEcheance.A_VENIR THEN 1
                WHEN e.statut = com.example.pfebtk.echeancier.entity.StatutEcheance.EN_RETARD THEN 2
                WHEN e.statut = com.example.pfebtk.echeancier.entity.StatutEcheance.PAYE THEN 3
                ELSE 4
            END,
            e.dateEcheance ASC
    """)
    List<Echeancier> findAllOrderByStatut();

    List<Echeancier> findByPayeFalseAndDateEcheanceBefore(LocalDate date);
}
