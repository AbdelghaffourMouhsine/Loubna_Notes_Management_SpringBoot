package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.entity.JournalApplication;
import com.example.gestion_de_notes.repository.JournalApplicationRepository;
import com.example.gestion_de_notes.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@Transactional
public class JournalApplicationService {
    
    @Autowired
    private JournalApplicationRepository journalRepository;
    
    @Autowired(required = false)
    private HttpServletRequest request;
    
    /**
     * Enregistre une action avec l'utilisateur connecté
     */
    public void enregistrerAction(String action, String details) {
        CompteUtilisateur utilisateurConnecte = SecurityUtils.getCurrentUser();
        if (utilisateurConnecte != null) {
            enregistrerAction(utilisateurConnecte, action, details);
        }
        // Si aucun utilisateur connecté, on ignore l'enregistrement
    }
    
    public void enregistrerAction(CompteUtilisateur utilisateur, String action, String details) {
        JournalApplication journal = new JournalApplication();
        journal.setUtilisateur(utilisateur);
        journal.setActionEffectuee(action);
        journal.setDetails(details);
        
        // Récupérer l'adresse IP si disponible
        if (request != null) {
            String adresseIp = getClientIpAddress();
            journal.setAdresseIp(adresseIp);
        }
        
        journalRepository.save(journal);
    }
    
    public List<JournalApplication> findByUtilisateur(Long utilisateurId) {
        return journalRepository.findByUtilisateurIdOrderByDateDesc(utilisateurId);
    }
    
    public List<JournalApplication> findByAction(String action) {
        return journalRepository.findByActionContaining(action);
    }
    
    private String getClientIpAddress() {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}
