package com.example.gestion_de_notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiliereDTO {
    
    private Long idFiliere;
    
    @NotBlank(message = "L'alias est obligatoire")
    @Size(max = 50, message = "L'alias ne doit pas dépasser 50 caractères")
    private String alias;
    
    @NotBlank(message = "L'intitulé est obligatoire")
    @Size(max = 200, message = "L'intitulé ne doit pas dépasser 200 caractères")
    private String intitule;
    
    @NotNull(message = "L'année d'accréditation est obligatoire")
    private Integer anneeAccreditation;
    
    private Integer anneeFinAccreditation;
    
    private Long idCoordonnateur;
    
    private String nomCoordonnateur;
}
