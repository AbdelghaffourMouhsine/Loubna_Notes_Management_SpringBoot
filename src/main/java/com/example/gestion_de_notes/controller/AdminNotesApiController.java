package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.service.EtudiantService;
import com.example.gestion_de_notes.service.NiveauService;
import com.example.gestion_de_notes.repository.HistoriqueModificationEtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notes")
@PreAuthorize("hasRole('ADMIN_NOTES')")
public class AdminNotesApiController {
    
    @Autowired
    private EtudiantService etudiantService;
    
    @Autowired
    private NiveauService niveauService;
    
    @Autowired
    private HistoriqueModificationEtudiantRepository historiqueRepository;
    
    /**
     * Endpoint pour récupérer les statistiques du dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total des étudiants
            long totalEtudiants = etudiantService.findAll().size();
            stats.put("totalEtudiants", totalEtudiants);
            
            // Nouveaux étudiants cette année (simulation)
            String anneeActuelle = getAnneeUniversitaireActuelle();
            // Cette requête devrait être optimisée dans un vrai projet
            long nouveauxEtudiants = etudiantService.findAll().stream()
                .mapToLong(dto -> {
                    // Logique simplifiée - à améliorer avec une vraie requête
                    return 0L;
                })
                .sum();
            stats.put("nouveauxEtudiants", Math.min(totalEtudiants / 4, 500)); // Simulation
            
            // Niveaux actifs
            long niveauxActifs = niveauService.findAll().size();
            stats.put("niveauxActifs", niveauxActifs);
            
            // Modifications récentes (dernières 24h)
            LocalDateTime hier = LocalDateTime.now().minusDays(1);
            LocalDateTime maintenant = LocalDateTime.now();
            long modificationsRecentes = historiqueRepository
                .findByDateModificationBetween(hier, maintenant).size();
            stats.put("modificationsRecentes", modificationsRecentes);
            
            stats.put("success", true);
            
        } catch (Exception e) {
            stats.put("success", false);
            stats.put("error", "Erreur lors du chargement des statistiques");
        }
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Endpoint pour récupérer les actions récentes
     */
    @GetMapping("/recent-actions")
    public ResponseEntity<Map<String, Object>> getRecentActions() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Actions récentes (dernières 10)
            LocalDateTime unJourAuparavant = LocalDateTime.now().minusDays(1);
            LocalDateTime maintenant = LocalDateTime.now();
            
            var actionsRecentes = historiqueRepository
                .findByDateModificationBetween(unJourAuparavant, maintenant)
                .stream()
                .limit(10)
                .map(h -> {
                    Map<String, Object> action = new HashMap<>();
                    action.put("type", "Modification étudiant");
                    action.put("description", "Modification de " + h.getEtudiant().getNomComplet());
                    action.put("date", h.getDateModification());
                    action.put("utilisateur", h.getUtilisateurModification() != null ? 
                        h.getUtilisateurModification().getLogin() : "Système");
                    return action;
                })
                .toList();
            
            result.put("actions", actionsRecentes);
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Erreur lors du chargement des actions récentes");
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Endpoint pour les années universitaires disponibles
     */
    @GetMapping("/annees-universitaires")
    public ResponseEntity<Map<String, Object>> getAnneesUniversitaires() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Cette méthode devrait être implémentée dans le repository
            // Pour l'instant, on simule
            var annees = java.util.List.of(
                "2024/2025",
                "2023/2024", 
                "2022/2023",
                "2021/2022"
            );
            
            result.put("annees", annees);
            result.put("anneeActuelle", getAnneeUniversitaireActuelle());
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Erreur lors du chargement des années universitaires");
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Utilitaire pour obtenir l'année universitaire actuelle
     */
    private String getAnneeUniversitaireActuelle() {
        int anneeActuelle = LocalDateTime.now().getYear();
        int moisActuel = LocalDateTime.now().getMonthValue();
        
        // Si on est entre septembre et décembre, l'année universitaire a commencé cette année
        // Sinon, elle a commencé l'année précédente
        if (moisActuel >= 9) {
            return anneeActuelle + "/" + (anneeActuelle + 1);
        } else {
            return (anneeActuelle - 1) + "/" + anneeActuelle;
        }
    }
}
