package com.example.pfebtk.demande.dto;

import com.example.pfebtk.annonce.dto.AnnonceResp;
import com.example.pfebtk.annonce.entity.ConventionType;
import com.example.pfebtk.demande.entity.DemandeStatut;
import jakarta.persistence.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DemandeResp(

        Long idDemande,
        LocalDateTime dateDemande,
        //Employé

        String userLib,


        //Annonce
        AnnonceResp annonce,

        //Convention signée
        String conventionSignePath,


        //Détails demande
        String codeClient,
        String numeroTel,
        String frequence,
        Integer duree,
        int jourPrelevement,
        String commentaire,


        LocalDate dateDebut,
        LocalDate dateFin,
        Integer franchise,
        //Statut
        DemandeStatut statut,

        //Amicale
        LocalDateTime dateDecisionAmicale,
        String remarqueRespAmicale

) {
}
