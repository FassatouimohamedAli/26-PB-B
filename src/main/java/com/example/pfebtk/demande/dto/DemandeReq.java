package com.example.pfebtk.demande.dto;

import org.springframework.web.multipart.MultipartFile;

public record DemandeReq(
        Long annonce ,
        String codeClient,
        String numeroTel,
        String commentaire,
        MultipartFile conventionSigne
) {
}
