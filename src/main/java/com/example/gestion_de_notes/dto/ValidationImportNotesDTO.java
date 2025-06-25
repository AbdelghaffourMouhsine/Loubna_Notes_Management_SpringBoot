package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationImportNotesDTO {
    
    private boolean valide;
    private String messageErreur;
    
    public ValidationImportNotesDTO(boolean valide) {
        this.valide = valide;
        this.messageErreur = null;
    }
}
