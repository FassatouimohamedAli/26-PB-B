package com.example.pfebtk.demande.service;

import com.example.pfebtk.annonce.dto.AnnonceMapper;
import com.example.pfebtk.annonce.dto.event.WsEvent;
import com.example.pfebtk.annonce.entity.Annonce;
import com.example.pfebtk.annonce.entity.Convention;
import com.example.pfebtk.annonce.entity.ConventionType;
import com.example.pfebtk.annonce.repository.AnnonceRepo;
import com.example.pfebtk.annonce.repository.ConventionRepo;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.repository.UserRepo;
import com.example.pfebtk.demande.dto.DecisionAmicaleRequest;
import com.example.pfebtk.demande.dto.DemandeMapper;
import com.example.pfebtk.demande.dto.DemandeReq;
import com.example.pfebtk.demande.dto.DemandeResp;
import com.example.pfebtk.demande.entity.DemandeAdhesion;
import com.example.pfebtk.demande.entity.DemandeStatut;
import com.example.pfebtk.demande.entity.Frequence;
import com.example.pfebtk.demande.exception.ConventionSignerException;
import com.example.pfebtk.demande.exception.DemandeDejaExisteException;
import com.example.pfebtk.demande.exception.PlusDePlaceException;
import com.example.pfebtk.demande.repository.DemandeAdhesionRepository;
import com.example.pfebtk.echeancier.service.EcheancierService;
import com.example.pfebtk.file.service.FileStorageService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class DemandeService {

    @Autowired
    private AnnonceRepo annonceRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ConventionRepo conventionRepo;
    @Autowired
    private FileStorageService fileService;

    @Autowired
    private DemandeAdhesionRepository demandeAdhesionRepository;
    @Autowired
    private DemandeMapper demandeMapper;

    @Autowired
    private AnnonceMapper annonceMapper ;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
@Autowired
private EcheancierService echeancierService ;



    // EMPLOYÉ


    @Transactional
    public DemandeResp creerDemande(String unix, DemandeReq req) {

        User user = userRepo.findByUnix(unix)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur introuvable : " + unix));


        Annonce annonce = annonceRepo.findById(req.annonce())
                .orElseThrow(() -> new RuntimeException(
                        "Annonce introuvable : " + req.annonce()));

        if (demandeAdhesionRepository.existsByUserUnixAndAnnonceIdAndStatutIn(unix ,annonce.getId(),List.of(DemandeStatut.EN_ATTENTE,DemandeStatut.VALIDEE)) ) {
            throw new DemandeDejaExisteException("Vous avez déjà une demande active pour cette annonce");
        }
        if (annonce.getMaxReservations() <= 0 ) {
            throw new PlusDePlaceException("Plus de places disponibles pour cette annonce");
        }
        // Sauvegarder convention PDF si fournie
        Convention convention = null;
        if (req.conventionSigne() != null
                && !req.conventionSigne().isEmpty()) {
            if (fileService.isPdf(req.conventionSigne())) {
                String filename = fileService.saveConventionSigned(req.conventionSigne());
                convention = Convention.builder()
                        .filePath(filename)
                        .type(ConventionType.SIGNER)
                        .build();

                convention = conventionRepo.save(convention);
            } else {
                throw new IllegalArgumentException(
                        "Seulement les PDF sont acceptés !");
            }
        }

        DemandeAdhesion demande = DemandeAdhesion.builder()
                .user(user)
                .annonce(annonce)
                .conventionSigne(convention)
                .codeClient(req.codeClient())
                .numeroTel(req.numeroTel())
                .dateDemande(LocalDateTime.now())
                .commentaire(req.commentaire())
                .statut(DemandeStatut.EN_ATTENTE)
                .build();


DemandeAdhesion demandeAdhesion = demandeAdhesionRepository.save(demande) ;
        messagingTemplate.convertAndSend(
                "/topic/demande",
                new WsEvent("CREATE", demandeMapper.toResponse(demandeAdhesion))
        );

        return demandeMapper.toResponse(demandeAdhesion);

    }

    @Transactional
    public void deleteDemande(Long id, String unix) {

        DemandeAdhesion demande = demandeAdhesionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Demande introuvable : " + id));



        // Vérifier que c'est bien l'employé qui supprime
        if (!demande.getUser().getUnix().equals(unix)) {
            throw new RuntimeException(
                    "Vous n'êtes pas autorisé à supprimer cette demande");
        }

        // Seulement EN_ATTENTE ou ANNULEE peuvent être supprimées
        if (demande.getStatut() != DemandeStatut.EN_ATTENTE
                && demande.getStatut() != DemandeStatut.ANNULEE) {
            throw new RuntimeException(
                    "Impossible de supprimer une demande déjà traitée");
        }

        // Supprimer le fichier PDF de la convention signée si présent
        if (demande.getConventionSigne() != null) {
            String filePath = demande.getConventionSigne().getFilePath();
            fileService.deleteConventionSigned(filePath);  // supprime le PDF du disque
            // CascadeType.ALL sur conventionSigne → supprimée automatiquement en BDD
        }

        demandeAdhesionRepository.delete(demande);
    }


    /**
     * Historique des demandes de l'employé connecté.
     */
    public List<DemandeResp> getMesDemandes(String unix) {

        User user = userRepo.findByUnix(unix)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur introuvable : " + unix));

        return demandeAdhesionRepository
                .findByUser_UnixOrderByDateDemandeDesc(user.getUnix())
                .stream()
                .map(demandeMapper::toResponse)
                .toList();
    }

    /**
     * Annuler une demande EN_ATTENTE.
     */
    @Transactional
    public DemandeResp annulerDemande(Long id) {

        DemandeAdhesion demande = demandeAdhesionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Demande introuvable : " + id));

        if (demande.getStatut() != DemandeStatut.EN_ATTENTE) {
            throw new RuntimeException(
                    "Impossible d'annuler une demande déjà traitée");
        }
        demande.setStatut(DemandeStatut.ANNULEE);

        messagingTemplate.convertAndSend(
                "/topic/demande",
                new WsEvent("CANCEL", demandeMapper.toResponse(demande))
        );
        return demandeMapper.toResponse(
                demandeAdhesionRepository.save(demande));
    }




    /**
     * Tableau de bord historiques .
     */
    public List<DemandeResp> getDemandes() {
        return demandeAdhesionRepository
                .findAllByOrderByDateDemandeDesc()
                .stream()
                .map(demandeMapper::toResponse)
                .toList();
    }


    /**
     * Tableau de bord Amicale : demandes EN_ATTENTE.
     */
    public List<DemandeResp> getDemandesEnAttente( ) {

        return demandeAdhesionRepository
                .findByStatut(DemandeStatut.EN_ATTENTE)
                .stream()
                .map(demandeMapper::toResponse)
                .toList();
    }


    /**
     * Amicale valide ou rejette.
     */
    @Transactional
    public DemandeResp decisionAmicale( DecisionAmicaleRequest req) {

        DemandeAdhesion demande = demandeAdhesionRepository.findById(req.idDemande())
                .orElseThrow(() -> new RuntimeException(
                        "Demande introuvable : " + req.idDemande()));

        // ✅ Vérification statut
        if (demande.getStatut() != DemandeStatut.EN_ATTENTE) {
            throw new RuntimeException("La demande doit être EN_ATTENTE");
        }

        Annonce a = demande.getAnnonce();

        // ❌ Si rejet → pas besoin de remplir autres champs
        if (!req.approuve() || a.getMaxReservations() <= 0) {
            demande.setStatut(DemandeStatut.REJETEE);
            demande.setRemarqueRespAmicale(req.remarqueRespAmicale());
            demande.setDateDecisionAmicale(LocalDateTime.now());
            DemandeAdhesion saved = demandeAdhesionRepository.save(demande);
            messagingTemplate.convertAndSend(
                    "/topic/demande",
                    new WsEvent("REJETEE", demandeMapper.toResponse(saved))
            );
            return demandeMapper.toResponse(saved);
        }

        //verif metier
        validateDuree(req.frequence(), req.duree());

        //maj
        demande.setFrequence(req.frequence());
        demande.setDuree(req.duree());
        demande.setJourPrelevement(req.jourPrelevement());

        demande.setStatut(DemandeStatut.VALIDEE);
        demande.setRemarqueRespAmicale(req.remarqueRespAmicale());
        demande.setDateDecisionAmicale(LocalDateTime.now());


        demande.setFranchise(req.franchise());
        //LocalDate dateDebutEffective = req.dateDebut().plusMonths(req.franchise());
        demande.setDateDebut(req.dateDebut());

        // dateFin calculated from effective start date
        LocalDate calculatedDateFin = calculDateFin(req.dateDebut(), req.frequence(), req.duree());
        demande.setDateFin(calculatedDateFin);


        a.setMaxReservations(a.getMaxReservations() - 1);
        annonceRepo.save(a);
        messagingTemplate.convertAndSend(
                "/topic/annonce",
                new WsEvent("UPDATE", annonceMapper.toResponse(a))
        );



        DemandeAdhesion saved = demandeAdhesionRepository.save(demande);
        //generrer les echancie si demande valdier !!
        echeancierService.generate(saved);
        messagingTemplate.convertAndSend(
                "/topic/demande",
                new WsEvent("VALIDEE", demandeMapper.toResponse(saved))
        );


        return demandeMapper.toResponse(saved);
    }



    private void validateDuree(Frequence freq, int duree) {

        switch (freq) {
            case TRIMESTRIEL -> {
                if (duree % 3 != 0)
                    throw new RuntimeException("Durée doit être multiple de 3");
            }
            case SEMESTRIEL -> {
                if (duree % 6 != 0)
                    throw new RuntimeException("Durée doit être multiple de 6");
            }
            case ANNUEL -> {
                if (duree % 12 != 0)
                    throw new RuntimeException("Durée doit être multiple de 12");
            }
        }
    }


    public Resource downloadConventionSigne(Long id) {
        DemandeAdhesion d = demandeAdhesionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("demande introuvable !"));

        if (d.getConventionSigne() == null) {
            throw new ConventionSignerException("Aucune convention pour cette demande !");
        }

        return fileService.loadConventionSigned(d.getConventionSigne().getFilePath());
    }


    private LocalDate calculDateFin(LocalDate debut, Frequence freq, int duree) {

        return switch (freq) {
            case MENSUEL, TRIMESTRIEL, SEMESTRIEL ->
                    debut.plusMonths(duree);

            case ANNUEL ->
                    debut.plusMonths(duree); // duree est déjà en mois
        };
    }

}


