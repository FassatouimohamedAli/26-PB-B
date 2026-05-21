package com.example.pfebtk.demande.entity;

public enum DemandeStatut {


    /** Demande soumise par l'employé, en attente de décision Amicale */
    EN_ATTENTE,

    /** Approuvée par le responsable Amicale, en attente de décision RH */
    VALIDEE,

    /** Rejetée par le responsable Amicale */
    REJETEE,


    /** Annulée par l'employé lui-même */
    ANNULEE
}
