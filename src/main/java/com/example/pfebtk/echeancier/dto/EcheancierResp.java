package com.example.pfebtk.echeancier.dto;

import com.example.pfebtk.demande.dto.DemandeResp;
import com.example.pfebtk.echeancier.entity.StatutEcheance;
import lombok.Builder;

import java.time.LocalDate;
@Builder

public record EcheancierResp(

        Long id,

        // date prévue de paiement
        LocalDate dateEcheance,

        // A_VENIR / EN_RETARD / PAYE
        StatutEcheance statut,

        // simulation paiement
        boolean paye,

        // date paiement réel
        LocalDate datePaiement,

        // relation demande
        DemandeResp demande



) {
}
