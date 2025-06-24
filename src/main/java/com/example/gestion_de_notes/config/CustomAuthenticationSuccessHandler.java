package com.example.gestion_de_notes.config;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        CompteUtilisateur user = (CompteUtilisateur) authentication.getPrincipal();
        
        // Rediriger vers le dashboard approprié selon le rôle
        String redirectUrl = switch (user.getRole()) {
            case ADMIN_USER -> "/admin/users/dashboard";
            case ADMIN_NOTES -> "/admin/notes/dashboard";
            case ADMIN_SP -> "/admin/structures/dashboard";
            default -> "/";
        };
        
        response.sendRedirect(redirectUrl);
    }
}
