package com.example.pfebtk.demande.dto;

import com.example.pfebtk.annonce.dto.AnnonceMapper;
import com.example.pfebtk.annonce.entity.Annonce;
import com.example.pfebtk.annonce.entity.Convention;
import com.example.pfebtk.demande.entity.DemandeAdhesion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DemandeMapper {

    @Autowired
    private AnnonceMapper annonceMapper; ;

    public DemandeResp toResponse(DemandeAdhesion d) {

        Annonce a = d.getAnnonce();

        return new DemandeResp(
                d.getIdDemande(),
                d.getDateDemande(),
                //Employé : seulement le nom
                d.getUser().getLib(),

                //Annonce complète
                annonceMapper.toResponse(a),

                d.getConventionSigne() != null ? d.getConventionSigne().getFilePath() : null,  // null si pas de convention

                //Détails demande
                d.getCodeClient(),
                d.getNumeroTel(),
                d.getFrequence() != null
                        ? d.getFrequence().name()        : null,
                d.getDuree(),
                d.getJourPrelevement(),
                d.getCommentaire(),

                d.getDateDebut(),
                d.getDateFin(),
                d.getFranchise(),
                //Statut
                d.getStatut(),

                //Amicale
                d.getDateDecisionAmicale(),
                d.getRemarqueRespAmicale()
        );
    }
}
