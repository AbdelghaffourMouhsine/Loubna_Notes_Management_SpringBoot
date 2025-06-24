package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "niveau")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Niveau {
    
    @Id
    @Column(name = "id_niveau")
    private Long idNiveau;
    
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 200, message = "Le libellé ne doit pas dépasser 200 caractères")
    @Column(name = "libelle", length = 200, nullable = false)
    private String libelle;
    
    @NotBlank(message = "L'alias est obligatoire")
    @Size(max = 50, message = "L'alias ne doit pas dépasser 50 caractères")
    @Column(name = "alias", length = 50, nullable = false, unique = true)
    private String alias;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_niveau_suivant")
    private Niveau niveauSuivant;
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "filiere_niveau",
        joinColumns = @JoinColumn(name = "id_niveau"),
        inverseJoinColumns = @JoinColumn(name = "id_filiere")
    )
    private List<Filiere> filieres;
    
    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Module> modules;
    
    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscription> inscriptions;
    
    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParametreValidation> parametresValidation;
}
