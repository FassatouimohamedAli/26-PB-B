package com.example.pfebtk.echeancier.dto;

import com.example.pfebtk.demande.dto.DemandeMapper;
import com.example.pfebtk.demande.entity.DemandeAdhesion;
import com.example.pfebtk.echeancier.entity.Echeancier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Component
public class EcheancierMapper {
    @Autowired
    private DemandeMapper demandeMapper;
    // =====================================================
    // ENTITY -> DTO
    // =====================================================
    public EcheancierResp toResponse(
            Echeancier e
    ) {

        DemandeAdhesion d = e.getDemande();
        return new EcheancierResp(

                e.getId(),

                e.getDateEcheance(),

                e.getStatut(),

                e.isPaye(),

                e.getDatePaiement(),

                demandeMapper.toResponse(d)
        );
    }
}