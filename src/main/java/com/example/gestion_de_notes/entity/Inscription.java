package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inscription")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscription")
    private Long idInscription;
    
    @NotNull(message = "L'étudiant est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etudiant", nullable = false)
    private Etudiant etudiant;
    
    @NotNull(message = "Le niveau est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_niveau", nullable = false)
    private Niveau niveau;
    
    @NotBlank(message = "L'année universitaire est obligatoire")
    @Column(name = "annee_universitaire", nullable = false)
    private String anneeUniversitaire;
    
    @NotNull(message = "Le type d'inscription est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_inscription", nullable = false)
    private TypeInscription typeInscription;
    
    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;
    
    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InscriptionModule> inscriptionsModule;
    
    @PrePersist
    protected void onCreate() {
        dateInscription = LocalDateTime.now();
    }
}
