package com.example.pfebtk.echeancier.service;

import com.example.pfebtk.annonce.dto.event.WsEvent;
import com.example.pfebtk.auth.service.email.EmailService;
import com.example.pfebtk.demande.entity.DemandeAdhesion;
import com.example.pfebtk.echeancier.dto.EcheancierMapper;
import com.example.pfebtk.echeancier.dto.EcheancierResp;
import com.example.pfebtk.echeancier.entity.Echeancier;
import com.example.pfebtk.echeancier.entity.StatutEcheance;
import com.example.pfebtk.echeancier.repository.EcheancierRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EcheancierService {
    @Autowired
    private EcheancierRepo repo;
    @Autowired
    private EcheancierMapper mapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EmailService emailService;

    // GENERATION AUTOMATIQUE

    @Transactional
    public void generate(DemandeAdhesion d) {

        // debut paiement
        // si franchise = 1 mois
        // -> debut + 1 mois

        LocalDate start = d.getDateDebut()
                .plusMonths(
                        d.getFranchise() == null
                                ? 0
                                : d.getFranchise()
                );


        // fin contrat

        LocalDate end = d.getDateFin();


        // step selon frequence

        int step = switch (d.getFrequence()) {

            case MENSUEL -> 1;

            case TRIMESTRIEL -> 3;

            case SEMESTRIEL -> 6;

            case ANNUEL -> 12;
        };


        // alignement jour prelevement

        LocalDate current =
                alignJP(start, d.getJourPrelevement());


        // generation echeanciers

        while (!current.isAfter(end)) {

            Echeancier e = new Echeancier();

            e.setDemande(d);

            e.setDateEcheance(current);

            e.setPaye(false);

            e.setStatut(StatutEcheance.A_VENIR);

            Echeancier saved = repo.save(e);


            // WEBSOCKET TEMPS REEL
            messagingTemplate.convertAndSend(
                    "/topic/echeancier",
                    new WsEvent(
                            "CREATE",
                            mapper.toResponse(saved)
                    )
            );

            // prochaine echeance

            current = alignJP(
                    current.plusMonths(step),
                    d.getJourPrelevement()
            );
        }
    }

    // PAIEMENT SIMULE
    // RESPONSABLE CLIQUE "PAYE"

    @Transactional
    public void payer(Long id) {

        Echeancier e = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Echeancier introuvable"
                        ));

        e.setPaye(true);

        e.setDatePaiement(LocalDate.now());

        e.setStatut(StatutEcheance.PAYE);

        repo.save(e);

    }

    // UPDATE STATUS

    @Scheduled(fixedRate = 3000)
    @Transactional
    public void updateStatus() {
        // today = LocalDate.now().plusMonths(14);
        LocalDate today = LocalDate.now();

        List<Echeancier> list = repo.findAll();

        for (Echeancier e : list) {

            // PAYE
            if (e.isPaye()) {

                if (e.getStatut() != StatutEcheance.PAYE) {

                    e.setStatut(StatutEcheance.PAYE);

                    repo.save(e);
                }
            }

            // RETARD
            else if (e.getDateEcheance().isBefore(today)) {

                if (e.getStatut() != StatutEcheance.EN_RETARD) {

                    e.setStatut(StatutEcheance.EN_RETARD);

                    repo.save(e);

                    messagingTemplate.convertAndSend(
                            "/topic/echeancier",
                            new WsEvent(
                                    "RETARD",
                                    mapper.toResponse(e)
                            )
                    );
                }
            }

            // A VENIR
            else {

                if (e.getStatut() != StatutEcheance.A_VENIR) {

                    e.setStatut(StatutEcheance.A_VENIR);

                    repo.save(e);
                }
            }
        }
    }

    // GET RETARDS

    public List<EcheancierResp> getRetards() {

        return repo.findAll()
                .stream()
                .filter(e ->
                        e.getStatut()
                                == StatutEcheance.EN_RETARD
                )
                .map(mapper::toResponse)
                .toList();


    }
    // GET PAR DEMANDE

    public List<EcheancierResp> getByDemande(
            Long demandeId
    ) {

        updateStatus();

        return repo.findByDemande_IdDemande(demandeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    // GET Alll

    public List<EcheancierResp> getAll() {
        updateStatus();
        return repo.findAllOrderByStatut()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    // ALIGNEMENT JOUR PRELEVEMENT

    private LocalDate alignJP(
            LocalDate date,
            int jp
    ) {

        int day = Math.min(
                jp,
                date.lengthOfMonth()
        );

        return date.withDayOfMonth(day);
    }


    @Scheduled(cron = "0 0 8 * * MON")
    @Transactional
    public void notifyRetardsGrouped() {

        List<Echeancier> retards =
                repo.findByPayeFalseAndDateEcheanceBefore(LocalDate.now());

        Map<String, List<Echeancier>> grouped =
                retards.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getDemande().getUser().getEmail()
                        ));

        for (Map.Entry<String, List<Echeancier>> entry : grouped.entrySet()) {

            emailService.sendRetardEcheancierGrouped(
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }
}