package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantNotesDTO {
    
    private Long etudiantId;
    private String cne;
    private String nom;
    private String prenom;
    private String nomComplet;
    
    // Notes par élément (clé = idElement, valeur = note)
    private Map<Long, BigDecimal> notesByElement;
    
    // Moyenne du module
    private BigDecimal moyenne;
    
    // Statut de validation (V, R, NV)
    private String validation;
    
    // Informations supplémentaires
    private String moduleCode;
    private String moduleTitle;
    private String elementsDetails;
    private String niveauAlias;
    private String anneeUniversitaire;
    private Boolean needsRattrapage;
    
    public EtudiantNotesDTO(Long etudiantId, String cne, String nom, String prenom) {
        this.etudiantId = etudiantId;
        this.cne = cne;
        this.nom = nom;
        this.prenom = prenom;
        this.nomComplet = nom + " " + prenom;
    }
}
