package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflitDonneesDTO {
    private String cne;
    private String nomFichier;
    private String prenomFichier;
    private String nomBase;
    private String prenomBase;
    private boolean mettreAJour;
    private int numeroLigne;
    
    public String getDonneesFichier() {
        return nomFichier + ", " + prenomFichier;
    }
    
    public String getDonneesBase() {
        return nomBase + ", " + prenomBase;
    }
}