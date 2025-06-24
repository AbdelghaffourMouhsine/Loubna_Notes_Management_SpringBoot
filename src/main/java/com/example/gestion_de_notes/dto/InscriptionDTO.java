package com.example.gestion_de_notes.dto;

import com.example.gestion_de_notes.entity.TypeInscription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionDTO {
    
    private Long idInscription;
    
    @NotNull(message = "L'étudiant est obligatoire")
    private Long idEtudiant;
    
    private String cneEtudiant;
    
    private String nomCompletEtudiant;
    
    @NotNull(message = "Le niveau est obligatoire")
    private Long idNiveau;
    
    private String aliasNiveau;
    
    private String libelleNiveau;
    
    @NotBlank(message = "L'année universitaire est obligatoire")
    private String anneeUniversitaire;
    
    @NotNull(message = "Le type d'inscription est obligatoire")
    private TypeInscription typeInscription;
    
    private LocalDateTime dateInscription;
}
