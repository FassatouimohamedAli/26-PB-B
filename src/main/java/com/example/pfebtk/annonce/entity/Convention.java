package com.example.pfebtk.annonce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Conventions")
public class Convention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PATH_FICHIER", nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private ConventionType type;
}
