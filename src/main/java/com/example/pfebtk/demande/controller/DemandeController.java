package com.example.pfebtk.demande.controller;


import com.example.pfebtk.annonce.dto.AnnonceResp;
import com.example.pfebtk.demande.dto.DecisionAmicaleRequest;
import com.example.pfebtk.demande.dto.DemandeReq;
import com.example.pfebtk.demande.dto.DemandeResp;
import com.example.pfebtk.demande.exception.SignatureNotDetectedException;
import com.example.pfebtk.demande.service.DemandeService;
import com.example.pfebtk.signatureDetection.service.SignatureDetectionService;
import com.example.pfebtk.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/emp")
public class DemandeController {

    @Autowired
    private DemandeService demandeService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SignatureDetectionService detectionService;


    @PostMapping(value = "/demande", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> creerDemande(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("annonce") String annonceId,
            @RequestPart("codeClient") String codeClient,
            @RequestPart("numerotel") String numerotel,
            @RequestPart(value = "commentaire", required = false)  String commentaire,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Problème de Header");
        }

        if (file != null && !file.isEmpty()) {
            try {
                boolean signed = detectionService.detectSignature(file);

                if (!signed) {
                    throw new SignatureNotDetectedException("La convention n'est pas signée");
                }

            } catch (Exception e) {
                throw new SignatureNotDetectedException("Erreur lors de la détection de signature");
            }
        }

        String token = authHeader.substring(7);
        String unix  = jwtUtil.extractUnix(token);

        DemandeReq req = new DemandeReq(
                Long.parseLong(annonceId),
                codeClient,
         numerotel,
                commentaire,
                file
        );

        return ResponseEntity.ok(demandeService.creerDemande(unix, req));
    }




    @GetMapping("/demandes")
    public ResponseEntity<?> getAll(@RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .badRequest()
                    .body("Problème de Header Authorization");
        }

        String token = authHeader.substring(7);
        String unix = jwtUtil.extractUnix(token);

        List<DemandeResp> result = demandeService.getMesDemandes(unix);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/annulerdemandes/{idDemande}")
    public ResponseEntity<?> Annullerdemande(@PathVariable Long idDemande) {
        DemandeResp DemandeAnnuler = demandeService.annulerDemande(idDemande);
        return ResponseEntity.ok(DemandeAnnuler);
    }



}
