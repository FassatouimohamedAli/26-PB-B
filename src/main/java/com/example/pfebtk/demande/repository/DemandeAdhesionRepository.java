package com.example.pfebtk.demande.repository;

import com.example.pfebtk.annonce.entity.Annonce;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.demande.entity.DemandeAdhesion;
import com.example.pfebtk.demande.entity.DemandeStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeAdhesionRepository extends JpaRepository<DemandeAdhesion, Long> {
    /** Toutes les demandes d'un employé via son unix (username) */
    List<DemandeAdhesion> findByUserUnix(String unix);

    /** Toutes les demandes ayant un statut précis */
    List<DemandeAdhesion> findByStatut(DemandeStatut statut);

    /** Toutes les demandes d'une annonce spécifique */
    List<DemandeAdhesion> findByAnnonceId(Long annonceId);

    /** Vérifier si un employé a déjà une demande active pour une annonce */
    boolean existsByUserUnixAndAnnonceIdAndStatutIn(
            String unix, Long annonceId, List<DemandeStatut> statuts);

    boolean existsByUserAndAnnonce(User user, Annonce annonce);

    List<DemandeAdhesion> findAllByOrderByDateDemandeDesc();
    List<DemandeAdhesion> findByUser_UnixOrderByDateDemandeDesc(String unix);
}
