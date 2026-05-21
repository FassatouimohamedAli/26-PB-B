package com.example.pfebtk.annonce.controller.resp;


import com.example.pfebtk.annonce.dto.AnnonceReq;
import com.example.pfebtk.annonce.dto.AnnonceResp;
import com.example.pfebtk.annonce.entity.AnnonceCategory;
import com.example.pfebtk.annonce.service.AnnonceService;
import com.example.pfebtk.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
//@RequiredArgsConstructor  prq injection par constructeur !!
@RequestMapping("/api/resp")
public class AnnonceController {

@Autowired
    private AnnonceService annonceService;
@Autowired
private JwtUtil  jwtUtil;

    //créer annonce
    @PostMapping(value = "/annonce", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAnnnce(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("titre") String titre,
            @RequestPart("description") String description,
            @RequestPart("categorie") String categorie,
            @RequestPart("conventionRequise") String conventionRequise,
            @RequestPart("prix") String prix,
            @RequestPart("maxReservations") String maxReservations,
            @RequestPart(value = "conventionFile", required = false) MultipartFile conventionFile ,
    @RequestPart(value = "image", required = false) MultipartFile image)
            throws Exception {
        BigDecimal prixDecimal = new BigDecimal(prix);
        AnnonceReq request = new AnnonceReq(
                titre,
                description,
                AnnonceCategory.valueOf(categorie),
                Boolean.parseBoolean(conventionRequise),
                prixDecimal,
                Integer.parseInt(maxReservations),
                conventionFile ,
                image
        );

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return  ResponseEntity.badRequest().body("problem de Header");
        }

        String token = authHeader.substring(7);
        System.out.println(token);
        String unix = jwtUtil.extractUnix(token);
        System.out.println(unix);

        return ResponseEntity.ok(annonceService.createAnnonce(unix, request));
    }

    @GetMapping("/annonces")
    public ResponseEntity<List<AnnonceResp>> getAll() {
        return ResponseEntity.ok(annonceService.getAllAnnonces());
    }

    @DeleteMapping("/annonce/{id}")
    public ResponseEntity<?> deleteAnnonce(
            @PathVariable Long id) {

        annonceService.deleteAnnonce(id);
        return ResponseEntity.ok("Annonce supprimée ");
    }

    @GetMapping("/annonce/one/{id}")
    public ResponseEntity<AnnonceResp> getById(@PathVariable Long id) {
        return ResponseEntity.ok(annonceService.getAnnonceById(id));
    }
    @PutMapping(value = "/annonce/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAnnonce(
            @PathVariable Long id,
            @RequestPart("titre") String titre,
            @RequestPart("description") String description,
            @RequestPart("categorie") String categorie,
            @RequestPart("conventionRequise") String conventionRequise,
            @RequestPart("prix") String  prix,
            @RequestPart("maxReservations") String maxReservations,
            @RequestPart(value = "conventionFile", required = false) MultipartFile conventionFile,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {
        BigDecimal prixDecimal = new BigDecimal(prix);

        AnnonceReq request = new AnnonceReq(
                titre,
                description,
                AnnonceCategory.valueOf(categorie),
                Boolean.parseBoolean(conventionRequise),
                prixDecimal,
                Integer.parseInt(maxReservations),

                conventionFile,
                image
        );

        return ResponseEntity.ok(annonceService.updateAnnonce(id, request));
    }

}
