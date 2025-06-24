package com.example.gestion_de_notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NiveauDTO {
    
    private Long idNiveau;
    
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 200, message = "Le libellé ne doit pas dépasser 200 caractères")
    private String libelle;
    
    @NotBlank(message = "L'alias est obligatoire")
    @Size(max = 50, message = "L'alias ne doit pas dépasser 50 caractères")
    private String alias;
    
    private Long idNiveauSuivant;
    
    private String niveauSuivantAlias;
}
