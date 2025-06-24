package com.example.gestion_de_notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantModuleAnneeDTO {
    
    private Long id;
    
    @NotNull(message = "L'enseignant est obligatoire")
    private Long idEnseignant;
    
    private String enseignantNom;
    
    private String enseignantPrenom;
    
    private String enseignantEmail;
    
    private Long idModule;
    
    private String moduleCode;
    
    private String moduleTitre;
    
    private Long idElement;
    
    private String elementCode;
    
    private String elementTitre;
    
    @NotBlank(message = "L'année universitaire est obligatoire")
    private String anneeUniversitaire;
}
