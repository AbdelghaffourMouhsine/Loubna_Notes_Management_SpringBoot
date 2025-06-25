package com.example.gestion_de_notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantCompletDTO {
    
    // Informations de base de l'étudiant
    private Long idEtudiant;
    private String cne;
    private String nom;
    private String prenom;
    private String nomComplet;
    
    // Informations sur l'inscription actuelle
    private InscriptionDTO inscriptionActuelle;
    
    // Informations sur le niveau actuel
    private NiveauDTO niveauActuel;
    
    // Informations sur la filière
    private FiliereDTO filiereActuelle;
    
    // Liste des modules du niveau
    private List<ModuleDTO> modulesNiveau;
    
    // Notes de l'étudiant pour l'année en cours
    private List<EtudiantNotesDTO> notesAnneeEnCours;
    
    // Statistiques
    private int nombreModulesValides;
    private int nombreModulesNonValides;
    private Double moyenneGenerale;
    private String statut; // ADMIS, AJOURNÉ, EN_COURS
    
    // Historique des inscriptions
    private List<InscriptionDTO> historiqueInscriptions;
}
