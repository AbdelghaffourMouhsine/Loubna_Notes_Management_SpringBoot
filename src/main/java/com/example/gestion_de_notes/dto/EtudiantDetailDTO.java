package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantDetailDTO {
    
    private Long idEtudiant;
    private String cne;
    private String nom;
    private String prenom;
    private String nomComplet;
    
    // Informations d'inscription
    private String niveauActuel;
    private String anneeUniversitaire;
    private String typeInscription;
    private LocalDateTime dateInscription;
    
    // Statistiques
    private int nombreInscriptions;
    private int nombreNotes;
    private Double moyenneGenerale;
    
    // Historique
    private int nombreModifications;
    private LocalDateTime derniereModification;
    private String dernierUtilisateurModification;
}
