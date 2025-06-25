package com.example.gestion_de_notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    
    // Nouvelles propriétés pour la gestion des filières
    @NotEmpty(message = "Au moins une filière doit être sélectionnée")
    private List<Long> idsFilieres;
    
    private List<FiliereDTO> filieres;
}
