package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationFichierRequestDTO {
    
    private Long moduleId;
    private Long niveauId;
    private String session; // "NORMALE" ou "RATTRAPAGE"
    private String anneeUniversitaire;
    private String semestre;
}
