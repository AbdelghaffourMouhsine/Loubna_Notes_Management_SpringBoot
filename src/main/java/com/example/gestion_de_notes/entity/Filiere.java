package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "filiere")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Filiere {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_filiere")
    private Long idFiliere;
    
    @NotBlank(message = "L'alias est obligatoire")
    @Size(max = 50, message = "L'alias ne doit pas dépasser 50 caractères")
    @Column(name = "alias", length = 50, nullable = false, unique = true)
    private String alias;
    
    @NotBlank(message = "L'intitulé est obligatoire")
    @Size(max = 200, message = "L'intitulé ne doit pas dépasser 200 caractères")
    @Column(name = "intitule", length = 200, nullable = false)
    private String intitule;
    
    @NotNull(message = "L'année d'accréditation est obligatoire")
    @Column(name = "annee_accreditation", nullable = false)
    private Integer anneeAccreditation;
    
    @Column(name = "annee_fin_accreditation")
    private Integer anneeFinAccreditation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coordonnateur")
    private Personne coordonnateur;
    
    @ManyToMany(mappedBy = "filieres", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Niveau> niveaux;
    
    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParametreValidation> parametresValidation;
}
