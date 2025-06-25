package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationImportDTO {
    private boolean success;
    private String message;
    private List<String> errors = new ArrayList<>();
    private List<ConflitDonneesDTO> conflits = new ArrayList<>();
    private String sessionId; // Pour maintenir l'état entre les requêtes
    private String fileName;
    private String anneeUniversitaire;
    
    public boolean hasConflits() {
        return !conflits.isEmpty();
    }
}
