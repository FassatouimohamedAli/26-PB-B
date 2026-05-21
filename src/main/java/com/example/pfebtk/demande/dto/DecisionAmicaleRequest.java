package com.example.pfebtk.demande.dto;

import com.example.pfebtk.demande.entity.Frequence;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record DecisionAmicaleRequest(
        long idDemande,
        Frequence frequence,
        int jourPrelevement,
        int duree ,
        boolean approuve,
        String remarqueRespAmicale,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateDebut,
        Integer franchise
) {
}
