package com.example.pfebtk.annonce.dto;

import com.example.pfebtk.annonce.entity.AnnonceCategory;
import com.example.pfebtk.annonce.entity.ConventionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnnonceResp(

        Long id,
        String createdBy,               // unix du responsable
        String titre,
        String description,
        AnnonceCategory categorie,
        boolean conventionRequise,
        String conventionFilePath,      // null si pas de convention
        ConventionType conventionType,  // null si pas de convention
        String imagePath,
         BigDecimal prix,
        Integer maxReservations,
        LocalDateTime dateCreation
) {
}
