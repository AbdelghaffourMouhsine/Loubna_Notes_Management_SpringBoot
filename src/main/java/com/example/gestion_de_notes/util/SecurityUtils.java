package com.example.gestion_de_notes.util;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    
    /**
     * Récupère l'utilisateur connecté depuis le contexte de sécurité Spring
     * @return l'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public static CompteUtilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof CompteUtilisateur) {
            return (CompteUtilisateur) authentication.getPrincipal();
        }
        
        return null;
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur est connecté, false sinon
     */
    public static boolean isUserAuthenticated() {
        return getCurrentUser() != null;
    }
    
    /**
     * Récupère l'ID de l'utilisateur connecté
     * @return l'ID de l'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public static Long getCurrentUserId() {
        CompteUtilisateur user = getCurrentUser();
        return user != null ? user.getIdCompte() : null;
    }
}
