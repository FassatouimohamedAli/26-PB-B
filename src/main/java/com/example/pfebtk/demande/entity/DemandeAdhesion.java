package com.example.pfebtk.demande.entity;


import com.example.pfebtk.annonce.entity.Annonce;
import com.example.pfebtk.annonce.entity.Convention;
import com.example.pfebtk.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Entité principale représentant une demande d'adhésion à un produit.
 * Elle passe par deux niveaux de validation : Amicale puis RH.
 * Remplace l'ancienne entité Reservation.
 */


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DEMANDE_ADHESION")
public class DemandeAdhesion {
    /** Identifiant unique de la demande, auto-généré par Oracle */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDDEMANDE")
    private Long idDemande;


    /**
     * L'employé qui soumet la demande.
     * Jointure avec la table USER (déjà existante).
     * Remplace les anciens champs NOM_EMPLOYE, EMAIL_EMPLOYE, etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /**
     * L'annonce (produit) concerné par la demande.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ANNONCE_ID", nullable = false)
    private Annonce annonce;

    /**
     * Convention PDF signée par l'employé (optionnelle).
     * Contient le type et le chemin du fichier PDF.
     * Remplace l'ancien champ CONVENTION_A_SIGNER_PDF VARCHAR.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CONVENTION_SIGNE_ID")
    private Convention conventionSigne;

    // INFORMATIONS DE LA DEMANDE

    /**
     * Code client de l'employé (CLI).
     * Copié depuis User.cuti au moment de la demande.
     * Permet de garder une trace même si l'utilisateur change.
     */
    @Column(name = "CLI", length = 20)
    private String codeClient;


    /**
     * Date et heure de soumission de la demande par l'employé.
     * Rempli automatiquement à la création.
     */
    @Column(name = "DATEDEMANDE", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateDemande;

    /**
     * Montant souscrit par l'employé (ex: cotisation mensuelle).
     * precision=10 scale=2 → ex: 1500.50 DT
     */
//    @Column(name = "MONTANT", precision = 10, scale = 2)
//    private BigDecimal montant;

    /**
     * Fréquence de prélèvement choisie par l'employé.
     * Ex: MENSUEL, TRIMESTRIEL, ANNUEL , BIMENSUEL ,,'SEMESTRIEL'
     */
    @Column(name = "FREQUENCE", length = 255)
    @Enumerated(EnumType.STRING)
    private Frequence frequence;

    /**
     * Jour du mois pour le prélèvement automatique.
     * Ex: 5 → prélèvement le 5 de chaque mois
     */
    @Column(name = "JOUR_PRELEVEMENT")
    private int jourPrelevement;

    @Column(name = "FRANCHISE")
    private Integer franchise;
    /**
     * Commentaire libre saisi par l'employé lors de la demande.
     */
    @Column(name = "COMMENTAIRE", length = 500)
    private String commentaire;

    // STATUT GLOBAL

    /**
     * Statut actuel de la demande
     * Géré automatiquement par les services Amicale et RH
     * Voir enum DemandeStatut pour les valeurs possibles
     */
    @Column(name = "STATUT", length = 255)
    @Enumerated(EnumType.STRING)
    private DemandeStatut statut;

    // DÉCISION AMICALE (1er niveau de validation)

    /**
     * Date à laquelle le responsable Amicale a pris sa décision.
     * Null si pas encore traité.
     */
    @Column(name = "DATEDECISIONAMICALE")
    private LocalDateTime dateDecisionAmicale;

    /**
     * Remarque ou motif laissé par le responsable Amicale.
     * Obligatoire en cas de rejet, optionnel en cas de validation.
     */
    @Column(name = "REMARQUE_RESP_AMICALE", length = 1000)
    private String remarqueRespAmicale;

    @Column(name = "NUMERO_TEL_P", length = 20 ,nullable = false)
    private String numeroTel;

    @Column(name = "DUREE")
    private int duree;


    @Column(name = "DATE_D")
    private LocalDate dateDebut;

    @Column(name = "DATE_F")
    private LocalDate dateFin;





}
