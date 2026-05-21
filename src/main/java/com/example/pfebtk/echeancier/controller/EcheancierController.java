package com.example.pfebtk.echeancier.controller;

import com.example.pfebtk.echeancier.dto.EcheancierResp;
import com.example.pfebtk.echeancier.service.EcheancierService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resp")
public class EcheancierController {
    @Autowired
    private EcheancierService echeancierService;


    @GetMapping("/echeanciers")
    public List<EcheancierResp> getall() {

        return echeancierService.getAll();
    }

    // GET ECHEANCIERS D'UNE DEMANDE

    @GetMapping("/demande/{id}")
    public List<EcheancierResp> getByDemande(
            @PathVariable Long id
    ) {

        return echeancierService.getByDemande(id);
    }




    // PAYER

    @PatchMapping("/payer/{id}")
    public void payer(
            @PathVariable Long id
    ) {

        echeancierService.payer(id);
    }

    // GET RETARDS
    @GetMapping("/retards")
    public List<EcheancierResp> getRetards() {

        return echeancierService.getRetards();
    }

}
