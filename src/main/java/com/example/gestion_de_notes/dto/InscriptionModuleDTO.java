package com.example.gestion_de_notes.dto;

import com.example.gestion_de_notes.entity.StatutInscriptionModule;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionModuleDTO {
    
    private Long idInscriptionModule;
    
    @NotNull(message = "L'inscription est obligatoire")
    private Long idInscription;
    
    private String cneEtudiant;
    
    private String nomCompletEtudiant;
    
    @NotNull(message = "Le module est obligatoire")
    private Long idModule;
    
    private String codeModule;
    
    private String titreModule;
    
    @NotNull(message = "Le statut est obligatoire")
    private StatutInscriptionModule statut;
}
