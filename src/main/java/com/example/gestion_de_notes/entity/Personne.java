package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "personne")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Personne {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personne")
    private Long idPersonne;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    @Column(name = "prenom", length = 100, nullable = false)
    private String prenom;
    
    @Email(message = "L'adresse email doit être valide")
    @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
    @Column(name = "email", length = 150)
    private String email;
    
    @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
    @Column(name = "telephone", length = 20)
    private String telephone;
    
    @NotBlank(message = "Le CIN est obligatoire")
    @Size(max = 20, message = "Le CIN ne doit pas dépasser 20 caractères")
    @Column(name = "cin", length = 20, nullable = false, unique = true)
    private String cin;
    
    @OneToMany(mappedBy = "personne", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompteUtilisateur> comptes;
    
    public String getNomComplet() {
        return nom + " " + prenom;
    }
}
