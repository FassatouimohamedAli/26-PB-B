package com.example.pfebtk.demande.controller.resp;

import com.example.pfebtk.demande.dto.DecisionAmicaleRequest;
import com.example.pfebtk.demande.dto.DemandeResp;
import com.example.pfebtk.demande.service.DemandeService;
import com.example.pfebtk.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resp")
public class decisionController {

    @Autowired
    private DemandeService demandeService;
    @Autowired
    private JwtUtil jwtUtil;



    @PutMapping("/decisionamicale")
    public ResponseEntity<DemandeResp> decisionAmicale(
            @RequestBody DecisionAmicaleRequest req
    ) {
        DemandeResp response = demandeService.decisionAmicale(req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDemande(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Problème de Header");
        }

        String unix = jwtUtil.extractUnix(authHeader.substring(7));
        demandeService.deleteDemande(id, unix);
        return ResponseEntity.ok("Demande supprimée avec succès");
    }
//les demandes en attente
    @GetMapping("/demandes/attente")
    public ResponseEntity<List<DemandeResp>> getDemandesEnAttente() {
        return ResponseEntity.ok(demandeService.getDemandesEnAttente());
    }
    // tout les demandes
    @GetMapping("/demandes")
    public ResponseEntity<List<DemandeResp>> getAll() {
        return ResponseEntity.ok(demandeService.getDemandes());
    }



    @GetMapping("/demande/conventionSigne/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = demandeService.downloadConventionSigne(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
