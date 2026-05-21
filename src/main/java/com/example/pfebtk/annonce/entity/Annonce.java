package com.example.pfebtk.annonce.entity;

import com.example.pfebtk.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
@Table(name = "Annonces")
public class Annonce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User createdBy;

    @OneToOne
    @JoinColumn(name = "CONVENTION_ID")
    private Convention convention;



    @Column(name = "TITRE",length = 100, nullable = false)
    private String titre ;

    @Column(name = "DESCRIPTION", length = 500, nullable = false)
    private String description ;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORIE", nullable = false)
    private AnnonceCategory categorie;

    @Column(name = "CONVENTION_REQUISE", nullable = false)
    private boolean conventionRequise;

    @Column(name = "IMAGE")
    private String imagePath;

    @Column(name = "PRIX" , precision = 38, scale = 2, nullable = false)
    private BigDecimal prix;

    @Column(name = "MAX_RESERVATIONS",  nullable = false)
    private Integer maxReservations;



    @Column(name = "DATE_CREATION", nullable = false , columnDefinition = "TIMESTAMP")
    private LocalDateTime dateCreation;




}
