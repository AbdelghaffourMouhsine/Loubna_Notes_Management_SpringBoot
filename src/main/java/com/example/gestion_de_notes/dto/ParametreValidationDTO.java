package com.example.gestion_de_notes.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametreValidationDTO {
    
    private Long idParametre;
    
    @NotNull(message = "La filière est obligatoire")
    private Long idFiliere;
    
    @NotNull(message = "Le niveau est obligatoire")
    private Long idNiveau;
    
    @NotNull(message = "Le seuil de validation normale est obligatoire")
    @DecimalMin(value = "0.0", message = "Le seuil doit être supérieur ou égal à 0")
    @DecimalMax(value = "20.0", message = "Le seuil doit être inférieur ou égal à 20")
    private BigDecimal seuilValidationNormale;
    
    @NotNull(message = "Le seuil de validation rattrapage est obligatoire")
    @DecimalMin(value = "0.0", message = "Le seuil doit être supérieur ou égal à 0")
    @DecimalMax(value = "20.0", message = "Le seuil doit être inférieur ou égal à 20")
    private BigDecimal seuilValidationRattrapage;
    
    // Propriétés supplémentaires pour l'affichage
    private String filiereAlias;
    private String filiereIntitule;
    private String niveauAlias;
    private String niveauLibelle;
}
