package com.example.pfebtk.echeancier.controller;

import com.example.pfebtk.echeancier.dto.EcheancierResp;
import com.example.pfebtk.echeancier.service.EcheancierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emp")
public class empEcheancierService {
    @Autowired
    private EcheancierService echeancierService;
    // GET ECHEANCIERS D'UNE DEMANDE

    @GetMapping("/demandemp/{id}")
    public List<EcheancierResp> getDemande(
            @PathVariable Long id
    ) {

        return echeancierService.getByDemande(id);
    }
}
