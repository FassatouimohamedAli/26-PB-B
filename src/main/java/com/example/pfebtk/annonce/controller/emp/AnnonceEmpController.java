package com.example.pfebtk.annonce.controller.emp;

import com.example.pfebtk.annonce.dto.AnnonceResp;
import com.example.pfebtk.annonce.service.AnnonceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
//@RequiredArgsConstructor  prq injection par constructeur !!
@RequestMapping("/api/emp")
public class AnnonceEmpController {

    @Autowired
    private AnnonceService annonceService;

    @GetMapping("/annonces")
    public ResponseEntity<List<AnnonceResp>> getAll() {
        return ResponseEntity.ok(annonceService.getAllAnnonces());
    }

    //télécharger convention
    @GetMapping("/annonce/convention/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = annonceService.downloadConvention(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
