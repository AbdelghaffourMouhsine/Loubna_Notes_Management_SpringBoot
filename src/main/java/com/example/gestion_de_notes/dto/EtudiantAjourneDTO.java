package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantAjourneDTO {
    private Long idEtudiant;
    private String cne;
    private String nomComplet;
    private Long niveauActuel;
    private String libelleNiveauActuel;
    private Long niveauSuivant;
    private String libelleNiveauSuivant;
    private List<ModuleDTO> modulesNonValides = new ArrayList<>();
    private List<ModuleDTO> modulesNiveauSuivant = new ArrayList<>();
    private List<Long> modulesSupplementairesSelectionnes = new ArrayList<>();
}
