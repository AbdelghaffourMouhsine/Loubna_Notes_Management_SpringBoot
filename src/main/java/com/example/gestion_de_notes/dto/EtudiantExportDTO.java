package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantExportDTO {
    
    private String cne;
    private String nom;
    private String prenom;
    private String nomComplet;
    private String niveauActuel;
    private String filiere;
    private String anneeUniversitaire;
    private String typeInscription;
    private String dateInscription;
    private Integer nombreNotes;
    private String moyenneGenerale;
    private String statut; // Actif, Ajourné, etc.
}
