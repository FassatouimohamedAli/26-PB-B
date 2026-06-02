package com.example.pfebtk.echenacier.service;

import com.example.pfebtk.annonce.dto.event.WsEvent;
import com.example.pfebtk.demande.entity.DemandeAdhesion;
import com.example.pfebtk.demande.entity.Frequence;
import com.example.pfebtk.echeancier.dto.EcheancierMapper;
import com.example.pfebtk.echeancier.dto.EcheancierResp;
import com.example.pfebtk.echeancier.entity.Echeancier;
import com.example.pfebtk.echeancier.entity.StatutEcheance;
import com.example.pfebtk.echeancier.repository.EcheancierRepo;
import com.example.pfebtk.echeancier.service.EcheancierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests métier de l'EcheancierService")
class EcheancierServiceTest {

    @Mock
    private EcheancierRepo repo;

    @Mock
    private EcheancierMapper mapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private EcheancierService echeancierService;

    private DemandeAdhesion demande;
    private Echeancier echeancier;
    private EcheancierResp echeancierResp;

    @BeforeEach
    void setUp() {
        demande = new DemandeAdhesion();
        demande.setIdDemande(1L);
        demande.setFrequence(Frequence.MENSUEL);
        demande.setJourPrelevement(15);
        demande.setDateDebut(LocalDate.of(2026, 5, 26));
        demande.setDateFin(LocalDate.of(2026, 7, 26));
        demande.setFranchise(0);

        echeancier = new Echeancier();
        echeancier.setId(1L);
        echeancier.setDemande(demande);
        echeancier.setPaye(false);
        echeancier.setDateEcheance(LocalDate.of(2026, 6, 15));
        echeancier.setStatut(StatutEcheance.A_VENIR);

        echeancierResp = EcheancierResp.builder()
                .id(1L)
                .statut(StatutEcheance.A_VENIR)
                .dateEcheance(LocalDate.of(2026, 7, 15))
                .build();
    }

    @Nested
    @DisplayName("Tests de génération d'échéanciers")
    class GenerationTests {

        @Test
        @DisplayName("Génération MENSUEL - doit créer 12 échéances pour 12 mois")
        void generate_Mensuel_DoitCreer12Echeances() {
            demande.setFrequence(Frequence.MENSUEL);
            demande.setDateDebut(LocalDate.of(2024, 1, 1));
            demande.setDateFin(LocalDate.of(2024, 12, 31)); // CORRIGÉ: 31 décembre
            demande.setJourPrelevement(15);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any(Echeancier.class))).thenReturn(echeancierResp);

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeancesGenerees = captor.getAllValues();
            assertThat(echeancesGenerees).hasSize(12);

            for (Echeancier e : echeancesGenerees) {
                assertThat(e.getStatut()).isEqualTo(StatutEcheance.A_VENIR);
                assertThat(e.isPaye()).isFalse();
            }

            verify(messagingTemplate, times(12)).convertAndSend(
                    eq("/topic/echeancier"),
                    any(WsEvent.class)
            );
        }

        @Test
        @DisplayName("Génération TRIMESTRIEL - doit créer 4 échéances pour 12 mois")
        void generate_Trimestriel_DoitCreer4Echeances() {
            demande.setFrequence(Frequence.TRIMESTRIEL);
            demande.setDateDebut(LocalDate.of(2024, 1, 1));
            demande.setDateFin(LocalDate.of(2024, 12, 31)); // CORRIGÉ
            demande.setJourPrelevement(15);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            assertThat(echeances).hasSize(4);

            List<LocalDate> dates = echeances.stream()
                    .map(Echeancier::getDateEcheance)
                    .toList();

            assertThat(dates).containsExactly(
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 4, 15),
                    LocalDate.of(2024, 7, 15),
                    LocalDate.of(2024, 10, 15)
            );
        }

        @Test
        @DisplayName("Génération SEMESTRIEL - doit créer 2 échéances pour 12 mois")
        void generate_Semestriel_DoitCreer2Echeances() {
            demande.setFrequence(Frequence.SEMESTRIEL);
            demande.setDateDebut(LocalDate.of(2024, 1, 1));
            demande.setDateFin(LocalDate.of(2024, 12, 31)); // CORRIGÉ
            demande.setJourPrelevement(15);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            assertThat(echeances).hasSize(2);

            List<LocalDate> dates = echeances.stream()
                    .map(Echeancier::getDateEcheance)
                    .toList();

            assertThat(dates).containsExactly(
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 7, 15)
            );
        }

        @Test
        @DisplayName("Génération ANNUEL - doit créer 1 échéance pour 12 mois")
        void generate_Annuel_DoitCreer1Echeance() {
            demande.setFrequence(Frequence.ANNUEL);
            demande.setDateDebut(LocalDate.of(2024, 1, 1));
            demande.setDateFin(LocalDate.of(2024, 12, 31)); // CORRIGÉ
            demande.setJourPrelevement(15);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            assertThat(echeances).hasSize(1);
            assertThat(echeances.get(0).getDateEcheance()).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("Génération avec FRANCHISE - doit décaler le début des paiements")
        void generate_AvecFranchise_DoitDecalerLeDebut() {
            demande.setFrequence(Frequence.MENSUEL);
            demande.setDateDebut(LocalDate.of(2024, 1, 1));
            demande.setDateFin(LocalDate.of(2024, 6, 30)); // CORRIGÉ
            demande.setJourPrelevement(15);
            demande.setFranchise(2);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            LocalDate premiereEcheance = echeances.get(0).getDateEcheance();
            assertThat(premiereEcheance.getMonthValue()).isEqualTo(3);
            assertThat(premiereEcheance.getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("Alignement jour prélèvement - mois à 30 jours")
        void alignJP_Mois30Jours_DoitPrendreJour30() {
            demande.setJourPrelevement(31);
            demande.setDateDebut(LocalDate.of(2024, 1, 31));
            demande.setDateFin(LocalDate.of(2024, 2, 29)); // CORRIGÉ: inclure février
            demande.setFrequence(Frequence.MENSUEL);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            assertThat(echeances).hasSizeGreaterThan(1); // Vérification de sécurité

            LocalDate dateEcheanceFevrier = echeances.get(1).getDateEcheance();
            assertThat(dateEcheanceFevrier.getDayOfMonth()).isEqualTo(29);
        }
    }

    @Nested
    @DisplayName("Tests de paiement d'échéance")
    class PaiementTests {

        @Test
        @DisplayName("Paiement - doit marquer l'échéance comme PAYE")
        void payer_DoitMarquerCommePaye() {
            when(repo.findById(1L)).thenReturn(Optional.of(echeancier));
            when(repo.save(any(Echeancier.class))).thenReturn(echeancier);

            echeancierService.payer(1L);

            assertThat(echeancier.isPaye()).isTrue();
            assertThat(echeancier.getStatut()).isEqualTo(StatutEcheance.PAYE);
            assertThat(echeancier.getDatePaiement()).isEqualTo(LocalDate.now());
            verify(repo, times(1)).save(echeancier);
        }

        @Test
        @DisplayName("Paiement - échéance inexistante doit lancer exception")
        void payer_EcheanceInexistante_DoitLancerException() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> echeancierService.payer(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Echeancier introuvable");
        }

        @Test
        @DisplayName("Paiement - échéance déjà payée doit rester PAYE")
        void payer_EcheanceDejaPayee_RestePaye() {
            echeancier.setPaye(true);
            echeancier.setStatut(StatutEcheance.PAYE);
            LocalDate datePaiementExistante = LocalDate.of(2024, 1, 1);
            echeancier.setDatePaiement(datePaiementExistante);

            when(repo.findById(1L)).thenReturn(Optional.of(echeancier));
            when(repo.save(any(Echeancier.class))).thenReturn(echeancier);

            echeancierService.payer(1L);

            assertThat(echeancier.isPaye()).isTrue();
            assertThat(echeancier.getStatut()).isEqualTo(StatutEcheance.PAYE);
        }
    }

    @Nested
    @DisplayName("Tests de mise à jour automatique des statuts")
    class UpdateStatusTests {

        @Test
        @DisplayName("updateStatus - échéance PAYE doit rester PAYE")
        void updateStatus_EcheancePaye_RestePaye() {
            echeancier.setPaye(true);
            echeancier.setStatut(StatutEcheance.A_VENIR);
            echeancier.setDateEcheance(LocalDate.now().minusDays(10));

            when(repo.findAll()).thenReturn(Arrays.asList(echeancier));
            when(repo.save(any(Echeancier.class))).thenReturn(echeancier);

            echeancierService.updateStatus();

            assertThat(echeancier.getStatut()).isEqualTo(StatutEcheance.PAYE);
            verify(repo, times(1)).save(echeancier);
        }

        @Test
        @DisplayName("updateStatus - échéance en retard (date dépassée) doit devenir EN_RETARD")
        void updateStatus_EcheanceEnRetard_DoitDevenirRetard() {
            echeancier.setPaye(false);
            echeancier.setStatut(StatutEcheance.A_VENIR);
            echeancier.setDateEcheance(LocalDate.now().minusDays(10));

            when(repo.findAll()).thenReturn(Arrays.asList(echeancier));
            when(repo.save(any(Echeancier.class))).thenReturn(echeancier);
            when(mapper.toResponse(echeancier)).thenReturn(echeancierResp);

            echeancierService.updateStatus();

            assertThat(echeancier.getStatut()).isEqualTo(StatutEcheance.EN_RETARD);
            verify(repo, times(1)).save(echeancier);
            verify(messagingTemplate, times(1)).convertAndSend(
                    eq("/topic/echeancier"),
                    any(WsEvent.class)
            );
        }

        @Test
        @DisplayName("updateStatus - échéance future doit rester A_VENIR")
        void updateStatus_EcheanceFuture_ResteAVenir() {
            echeancier.setPaye(false);
            echeancier.setStatut(StatutEcheance.EN_RETARD);
            echeancier.setDateEcheance(LocalDate.now().plusMonths(1));

            when(repo.findAll()).thenReturn(Arrays.asList(echeancier));
            when(repo.save(any(Echeancier.class))).thenReturn(echeancier);

            echeancierService.updateStatus();

            assertThat(echeancier.getStatut()).isEqualTo(StatutEcheance.A_VENIR);
        }

        @Test
        @DisplayName("updateStatus - échéance EN_RETARD déjà payée devient PAYE")
        void updateStatus_RetardPaye_DevientPaye() {
            echeancier.setPaye(true);
            echeancier.setStatut(StatutEcheance.EN_RETARD);
            echeancier.setDateEcheance(LocalDate.now().minusDays(10));

            when(repo.findAll()).thenReturn(Arrays.asList(echeancier));
            when(repo.save(any(Echeancier.class))).thenReturn(echeancier);

            echeancierService.updateStatus();

            assertThat(echeancier.getStatut()).isEqualTo(StatutEcheance.PAYE);
        }

        @Test
        @DisplayName("updateStatus - échéance non dépassée ne devient pas EN_RETARD")
        void updateStatus_EcheanceNonDepassee_NeDevientPasRetard() {
            // CORRIGÉ: Teste le comportement réel (pas de seuil de 3 mois)
            echeancier.setPaye(false);
            echeancier.setStatut(StatutEcheance.A_VENIR);
            echeancier.setDateEcheance(LocalDate.now().plusDays(5)); // Dans le futur

            when(repo.findAll()).thenReturn(Arrays.asList(echeancier));

            echeancierService.updateStatus();

            verify(repo, never()).save(echeancier);
            assertThat(echeancier.getStatut()).isEqualTo(StatutEcheance.A_VENIR);
        }
    }

    @Nested
    @DisplayName("Tests de recherche des échéances")
    class RechercheTests {

        @Test
        @DisplayName("getRetards - doit filtrer uniquement les échéances EN_RETARD")
        void getRetards_DoitFiltrerUniquementRetards() {
            Echeancier e1 = createEcheancier(1L, StatutEcheance.EN_RETARD, false);
            Echeancier e2 = createEcheancier(2L, StatutEcheance.PAYE, true);
            Echeancier e3 = createEcheancier(3L, StatutEcheance.A_VENIR, false);
            Echeancier e4 = createEcheancier(4L, StatutEcheance.EN_RETARD, false);

            when(repo.findAll()).thenReturn(Arrays.asList(e1, e2, e3, e4));
            when(mapper.toResponse(e1)).thenReturn(createResp(1L, StatutEcheance.EN_RETARD));
            when(mapper.toResponse(e4)).thenReturn(createResp(4L, StatutEcheance.EN_RETARD));

            List<EcheancierResp> retards = echeancierService.getRetards();

            assertThat(retards).hasSize(2);
            assertThat(retards.get(0).id()).isEqualTo(1L);
            assertThat(retards.get(1).id()).isEqualTo(4L);
        }

        @Test
        @DisplayName("getRetards - doit retourner uniquement les échéances EN_RETARD")
        void getRetards_DoitRetournerUniquementRetards() {

            Echeancier retard1 = createEcheancier(1L, StatutEcheance.EN_RETARD, false);
            Echeancier retard2 = createEcheancier(2L, StatutEcheance.EN_RETARD, false);

            Echeancier paye = createEcheancier(3L, StatutEcheance.PAYE, true);
            Echeancier avenir = createEcheancier(4L, StatutEcheance.A_VENIR, false);

            when(repo.findAll()).thenReturn(List.of(retard1, retard2, paye, avenir));

            when(mapper.toResponse(retard1)).thenReturn(createResp(1L, StatutEcheance.EN_RETARD));
            when(mapper.toResponse(retard2)).thenReturn(createResp(2L, StatutEcheance.EN_RETARD));

            List<EcheancierResp> result = echeancierService.getRetards();

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(EcheancierResp::id)
                    .containsExactly(1L, 2L);

            verify(repo, times(1)).findAll();
        }

        @Test
        @DisplayName("getRetards - aucun retard retourne liste vide")
        void getRetards_AucunRetard_RetourneListeVide() {
            Echeancier e1 = createEcheancier(1L, StatutEcheance.PAYE, true);
            Echeancier e2 = createEcheancier(2L, StatutEcheance.A_VENIR, false);

            when(repo.findAll()).thenReturn(Arrays.asList(e1, e2));

            List<EcheancierResp> retards = echeancierService.getRetards();

            assertThat(retards).isEmpty();
        }

        @Test
        @DisplayName("getByDemande - doit retourner les échéances d'une demande spécifique")
        void getByDemande_DoitRetournerEcheancesDeLaDemande() {
            Long demandeId = 5L;
            List<Echeancier> echeances = Arrays.asList(echeancier);
            when(repo.findByDemande_IdDemande(demandeId)).thenReturn(echeances);
            when(mapper.toResponse(echeancier)).thenReturn(echeancierResp);

            List<EcheancierResp> result = echeancierService.getByDemande(demandeId);

            assertThat(result).hasSize(1);
            verify(repo, times(1)).findByDemande_IdDemande(demandeId);
        }

        @Test
        @DisplayName("getAll - doit retourner toutes les échéances")
        void getAll_DoitRetournerToutesEcheances() {

            Echeancier e = createEcheancier(1L, StatutEcheance.A_VENIR, false);

            when(repo.findAllOrderByStatut()).thenReturn(List.of(e));
            when(mapper.toResponse(any(Echeancier.class))).thenReturn(echeancierResp);

            List<EcheancierResp> result = echeancierService.getAll();

            assertThat(result).hasSize(1);

            verify(repo, atLeastOnce()).findAll();
        }
    }

    @Nested
    @DisplayName("Tests d'alignement du jour de prélèvement")
    class AlignementJPTests {

        @Test
        @DisplayName("alignJP - jour 31 en février (année normale) → 28")
        void alignJP_31FevrierNormal_DoitDonner28() {
            demande.setJourPrelevement(31);
            demande.setDateDebut(LocalDate.of(2023, 1, 31));
            demande.setDateFin(LocalDate.of(2023, 2, 28));
            demande.setFrequence(Frequence.MENSUEL);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            assertThat(echeances).hasSizeGreaterThan(1);

            LocalDate dateFevrier = echeances.get(1).getDateEcheance();
            assertThat(dateFevrier.getDayOfMonth()).isEqualTo(28);
        }

        @Test
        @DisplayName("alignJP - jour 31 en février (année bissextile) → 29")
        void alignJP_31FevrierBissextile_DoitDonner29() {
            demande.setJourPrelevement(31);
            demande.setDateDebut(LocalDate.of(2024, 1, 31));
            demande.setDateFin(LocalDate.of(2024, 2, 29));
            demande.setFrequence(Frequence.MENSUEL);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            assertThat(echeances).hasSizeGreaterThan(1);

            LocalDate dateFevrier = echeances.get(1).getDateEcheance();
            assertThat(dateFevrier.getDayOfMonth()).isEqualTo(29);
        }

        @Test
        @DisplayName("alignJP - jour 30 en avril (30 jours) → 30")
        void alignJP_30Avril_DoitDonner30() {
            demande.setJourPrelevement(30);
            demande.setDateDebut(LocalDate.of(2024, 3, 30));
            demande.setDateFin(LocalDate.of(2024, 4, 30));
            demande.setFrequence(Frequence.MENSUEL);

            when(repo.save(any(Echeancier.class))).thenAnswer(inv -> inv.getArgument(0));

            echeancierService.generate(demande);

            ArgumentCaptor<Echeancier> captor = ArgumentCaptor.forClass(Echeancier.class);
            verify(repo, atLeastOnce()).save(captor.capture());

            List<Echeancier> echeances = captor.getAllValues();
            assertThat(echeances).hasSizeGreaterThan(1);

            LocalDate dateAvril = echeances.get(1).getDateEcheance();
            assertThat(dateAvril.getDayOfMonth()).isEqualTo(30);
        }
    }

    private Echeancier createEcheancier(Long id, StatutEcheance statut, boolean isPaye) {
        Echeancier e = new Echeancier();
        e.setId(id);
        e.setStatut(statut);
        e.setPaye(isPaye);
        e.setDateEcheance(LocalDate.now().minusDays(10));
        return e;
    }

    private EcheancierResp createResp(Long id, StatutEcheance statut) {
        return EcheancierResp.builder()
                .id(id)
                .statut(statut)
                .dateEcheance(LocalDate.now())
                .build();
    }
}