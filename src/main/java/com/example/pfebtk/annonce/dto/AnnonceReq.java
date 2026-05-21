package com.example.pfebtk.annonce.dto;

import com.example.pfebtk.annonce.entity.AnnonceCategory;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnnonceReq(
        String titre,
        String description,
        AnnonceCategory categorie,
        boolean conventionRequise,
         BigDecimal  prix,
        Integer maxReservations,
        MultipartFile conventionFile,
        MultipartFile image
) {
}
