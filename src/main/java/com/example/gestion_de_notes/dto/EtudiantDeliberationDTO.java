package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantDeliberationDTO {
    
    private Long etudiantId;
    private String cne;
    private String nom;
    private String prenom;
    private String nomComplet;
    
    // Notes par module (clé = idModule, valeur = données du module)
    private Map<Long, ModuleNotesDTO> moduleNotes;
    
    // Moyenne générale de l'année
    private BigDecimal moyenneGenerale;
    
    // Rang dans la classe
    private Integer rang;
    
    // Informations sur le niveau
    private String niveauAlias;
    private String anneeUniversitaire;
    
    // Modules supplémentaires pour étudiants ajournés
    private List<ModuleNotesDTO> modulesSupplementaires;
    
    public EtudiantDeliberationDTO(Long etudiantId, String cne, String nom, String prenom) {
        this.etudiantId = etudiantId;
        this.cne = cne;
        this.nom = nom;
        this.prenom = prenom;
        this.nomComplet = nom + " " + prenom;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleNotesDTO {
        private Long moduleId;
        private String moduleCode;
        private String moduleTitle;
        private String enseignantNom;
        
        // Notes par élément du module
        private Map<Long, BigDecimal> elementNotes;
        
        // Moyenne du module
        private BigDecimal moyenneModule;
        
        // Validation du module
        private String validation; // V, R, NV
        
        // Si c'est un module supplémentaire (étudiant ajourné)
        private Boolean moduleSupplementaire = false;
    }
}
