package com.example.pfebtk.annonce.dto;

import com.example.pfebtk.annonce.entity.Annonce;
import org.springframework.stereotype.Component;

@Component
public class AnnonceMapper {
    public AnnonceResp toResponse(Annonce a) {
        return new AnnonceResp(
                a.getId(),
                a.getCreatedBy().getLib(),
                a.getTitre(),
                a.getDescription(),
                a.getCategorie(),
                a.isConventionRequise(),
                a.getConvention() != null ? a.getConvention().getFilePath() : null,  // null si pas de convention
                a.getConvention() != null ? a.getConvention().getType() : null,      // null si pas de convention
                a.getImagePath(),
                a.getPrix(),
                a.getMaxReservations(),
                a.getDateCreation()
        );
    }
}
