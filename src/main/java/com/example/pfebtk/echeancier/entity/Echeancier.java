package com.example.pfebtk.echeancier.entity;

import com.example.pfebtk.demande.entity.DemandeAdhesion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ECHEANCIER")
public class Echeancier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "DATE_ECHEANCE")
    private LocalDate dateEcheance;


    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT")
    private StatutEcheance statut;

    // 💰 simulation paiement (responsable clique "payer")
    @Column(name = "PAYE")
    private boolean paye;

    // 📅 date réelle de paiement
    @Column(name = "DATE_PAIEMENT")
    private LocalDate datePaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id")
    private DemandeAdhesion demande;



}
