package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantSearchDTO {
    
    private String cne;
    private String nom;
    private String prenom;
    private Long niveauId;
    private String niveauAlias;
    private String anneeUniversitaire;
    private String typeInscription;
    
    public boolean hasAnyCriteria() {
        return (cne != null && !cne.trim().isEmpty()) ||
               (nom != null && !nom.trim().isEmpty()) ||
               (prenom != null && !prenom.trim().isEmpty()) ||
               niveauId != null ||
               (anneeUniversitaire != null && !anneeUniversitaire.trim().isEmpty());
    }
}
