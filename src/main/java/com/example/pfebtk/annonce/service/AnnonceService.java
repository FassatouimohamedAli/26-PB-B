package com.example.pfebtk.annonce.service;

import com.example.pfebtk.annonce.dto.AnnonceMapper;
import com.example.pfebtk.annonce.dto.AnnonceReq;
import com.example.pfebtk.annonce.dto.AnnonceResp;
import com.example.pfebtk.annonce.dto.event.WsEvent;
import com.example.pfebtk.annonce.entity.Annonce;
import com.example.pfebtk.annonce.entity.Convention;
import com.example.pfebtk.annonce.entity.ConventionType;
import com.example.pfebtk.annonce.repository.AnnonceRepo;
import com.example.pfebtk.annonce.repository.ConventionRepo;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.repository.UserRepo;
import com.example.pfebtk.file.service.FileStorageService;
import com.example.pfebtk.image.service.ImageService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class AnnonceService {

    @Autowired
    private AnnonceRepo annonceRepo;
    @Autowired
    private ConventionRepo conventionRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FileStorageService fileService;
    @Autowired
    private ImageService imageService;

    @Autowired
    private AnnonceMapper annonceMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @Transactional
    public AnnonceResp createAnnonce(String unix, AnnonceReq request) throws Exception {

        User resp = userRepo.findByUnix(unix)
                .orElseThrow(() -> new RuntimeException("Responsable introuvable"));




        //image
        String imagePath = null;

        if (request.image() != null && !request.image().isEmpty()) {
            if (!imageService.isImage(request.image())) {
                throw new IllegalArgumentException("Format image invalide !");
            }
            imagePath = imageService.saveImage(request.image());
        }

        //convention
        Convention convention = null;

        if (request.conventionRequise()) {
            if (request.conventionFile() != null && !request.conventionFile().isEmpty()) {
                if (fileService.isPdf(request.conventionFile())) {
                    String filename = fileService.saveConvention(request.conventionFile());
                    convention = Convention.builder()
                            .filePath(filename)
                            .type(ConventionType.ORGINAL)
                            .build();
                    convention = conventionRepo.save(convention);
                    System.out.println("Convention saved : " + filename);
                } else {
                    throw new IllegalArgumentException("Seulement les PDF sont acceptés !");
                }
            } else {
                throw new IllegalArgumentException("Convention PDF obligatoire !");
            }
        } else {
            System.out.println("Pas de convention requise");
        }




        //annonce
        Annonce annonce = Annonce.builder()
                .createdBy(resp)
                .titre(request.titre())
                .description(request.description())
                .categorie(request.categorie())
                .conventionRequise(request.conventionRequise())
                .convention(convention) // null si pas requise
                .imagePath(imagePath)
                .prix(request.prix())
                .maxReservations(request.maxReservations())

                .dateCreation(LocalDateTime.now())
                .build();

        Annonce saved = annonceRepo.save(annonce);
        messagingTemplate.convertAndSend(
                "/topic/annonce",
                new WsEvent("CREATE", saved)
        );

        return annonceMapper.toResponse(saved) ;
    }

    //télécharger convention
    public Resource downloadConvention(Long id) {
        Annonce annonce = annonceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce introuvable !"));

        if (!annonce.isConventionRequise() || annonce.getConvention() == null) {
            throw new RuntimeException("Aucune convention pour cette annonce !");
        }

        return fileService.loadConvention(annonce.getConvention().getFilePath());
    }

    public List<AnnonceResp> getAllAnnonces() {
        return annonceRepo.findAllByOrderByDateCreationDesc()
                .stream()
                .map(annonceMapper::toResponse)
                .toList();
    }
    public AnnonceResp getAnnonceById(Long id) {
        Annonce annonce = annonceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce introuvable !"));
        return annonceMapper.toResponse(annonce);
    }


    //delete annonce
    @Transactional
    public void deleteAnnonce(Long id) {
        Annonce annonce = annonceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce introuvable !"));

        // supprimer fichier convention si existe
        if (annonce.getConvention() != null) {
            fileService.deleteConvention(annonce.getConvention().getFilePath());
            conventionRepo.delete(annonce.getConvention());
        }

        // supprimer image si existe
        if (annonce.getImagePath() != null) {
            imageService.deleteImage(annonce.getImagePath());
        }

        annonceRepo.delete(annonce);
        System.out.println("Annonce supprimée : " + id);

        messagingTemplate.convertAndSend(
                "/topic/annonce",
                new WsEvent("DELETE", id)
        );


    }



    @Transactional
    public AnnonceResp updateAnnonce(Long id, AnnonceReq request) throws Exception {

        Annonce annonce = annonceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce introuvable !"));



        // ── Image ──────────────────────────────────────────────
        if (request.image() != null && !request.image().isEmpty()) {
            if (!imageService.isImage(request.image())) {
                throw new IllegalArgumentException("Format image invalide !");
            }
            // Supprimer ancienne image
            if (annonce.getImagePath() != null) {
                imageService.deleteImage(annonce.getImagePath());
            }
            annonce.setImagePath(imageService.saveImage(request.image()));
        }

        // ── Convention ─────────────────────────────────────────
        if (request.conventionRequise()) {
            if (request.conventionFile() != null && !request.conventionFile().isEmpty()) {
                if (!fileService.isPdf(request.conventionFile())) {
                    throw new IllegalArgumentException("Seulement les PDF sont acceptés !");
                }
                // Supprimer ancienne convention
                if (annonce.getConvention() != null) {
                    fileService.deleteConvention(annonce.getConvention().getFilePath());
                    conventionRepo.delete(annonce.getConvention());
                }
                String filename = fileService.saveConvention(request.conventionFile());
                Convention convention = Convention.builder()
                        .filePath(filename)
                        .type(ConventionType.ORGINAL)
                        .build();
                annonce.setConvention(conventionRepo.save(convention));
            }
            // Si conventionRequise mais pas de nouveau fichier → on garde l'ancienne
        } else {
            // Convention plus requise → supprimer si elle existait
            if (annonce.getConvention() != null) {
                fileService.deleteConvention(annonce.getConvention().getFilePath());
                conventionRepo.delete(annonce.getConvention());
                annonce.setConvention(null);
            }
        }

        // ── Champs simples ─────────────────────────────────────
        annonce.setTitre(request.titre());
        annonce.setDescription(request.description());
        annonce.setCategorie(request.categorie());
        annonce.setConventionRequise(request.conventionRequise());
        annonce.setPrix(request.prix());
        annonce.setMaxReservations(request.maxReservations());


        messagingTemplate.convertAndSend(
                "/topic/annonce",
                new WsEvent("UPDATE", annonceMapper.toResponse(annonce))
        );

        return annonceMapper.toResponse(annonceRepo.save(annonce));
    }

}
