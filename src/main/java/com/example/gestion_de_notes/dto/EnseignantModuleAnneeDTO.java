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
    
    private String nomCompletEnseignant;
    
    @NotNull(message = "Le module est obligatoire")
    private Long idModule;
    
    private String codeModule;
    
    private String titreModule;
    
    private Long idElement;
    
    private String codeElement;
    
    private String titreElement;
    
    @NotBlank(message = "L'année universitaire est obligatoire")
    private String anneeUniversitaire;
}
