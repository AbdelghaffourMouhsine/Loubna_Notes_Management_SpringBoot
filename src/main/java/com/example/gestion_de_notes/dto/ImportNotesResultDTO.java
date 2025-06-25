package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportNotesResultDTO {
    
    private boolean succes;
    private String message;
    private int nbNotesImportees;
    private boolean necessiteConfirmation;
    private Map<String, String> metadonnees;
    
    public ImportNotesResultDTO(boolean succes, String message) {
        this.succes = succes;
        this.message = message;
        this.nbNotesImportees = 0;
        this.necessiteConfirmation = false;
    }
}
