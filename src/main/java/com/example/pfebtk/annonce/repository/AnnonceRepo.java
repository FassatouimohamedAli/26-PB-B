package com.example.pfebtk.annonce.repository;

import com.example.pfebtk.annonce.entity.Annonce;
import com.example.pfebtk.annonce.entity.AnnonceCategory;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.demande.entity.DemandeAdhesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnonceRepo extends JpaRepository<Annonce, Long> {

    // toutes les annonces triées par date création
    List<Annonce> findAllByOrderByDateCreationDesc();


    // annonces par categorie
    List<Annonce> findByCategorieOrderByDateCreationDesc(AnnonceCategory categorie);


}
