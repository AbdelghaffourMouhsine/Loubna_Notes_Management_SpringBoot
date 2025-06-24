package com.example.gestion_de_notes.dto;

import com.example.gestion_de_notes.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteUtilisateurDTO {
    
    private Long idCompte;
    private String login;
    private String motDePasseGenere;
    
    @NotNull(message = "Le rôle est obligatoire")
    private Role role;
    
    @NotNull(message = "La personne est obligatoire")
    private Long idPersonne;
    
    private String nomPersonne;
    private String prenomPersonne;
    private String cinPersonne;
    
    private Boolean enabled = true;
    private Boolean locked = false;
    
    public String getNomCompletPersonne() {
        return nomPersonne + " " + prenomPersonne;
    }
    
    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : "";
    }
}
