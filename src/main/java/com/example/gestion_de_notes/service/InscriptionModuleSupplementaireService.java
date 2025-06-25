package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.entity.*;
import com.example.gestion_de_notes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InscriptionModuleSupplementaireService {
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private InscriptionRepository inscriptionRepository;
    
    @Autowired
    private InscriptionModuleRepository inscriptionModuleRepository;
    
    @Autowired
    private JournalApplicationService journalService;
    
    /**
     * Inscrire un étudiant ajourné à des modules supplémentaires du niveau suivant
     */
    public boolean inscrireModulesSupplementaires(Long idEtudiant, List<Long> moduleIds, 
                                                 String anneeUniversitaire, CompteUtilisateur utilisateur) {
        
        Optional<Etudiant> etudiantOpt = etudiantRepository.findById(idEtudiant);
        if (!etudiantOpt.isPresent()) {
            return false;
        }
        
        Etudiant etudiant = etudiantOpt.get();
        
        // Récupérer l'inscription actuelle de l'étudiant pour cette année
        List<Inscription> inscriptions = inscriptionRepository.findByEtudiantAndAnnee(idEtudiant, anneeUniversitaire);
        if (inscriptions.isEmpty()) {
            return false;
        }
        
        // Prendre la première inscription (ou la plus récente)
        Inscription inscription = inscriptions.get(0);
        
        // Supprimer les anciennes inscriptions aux modules supplémentaires
        supprimerInscriptionsModulesSupplementaires(inscription);
        
        // Ajouter les nouvelles inscriptions
        int nbModulesAjoutes = 0;
        for (Long moduleId : moduleIds) {
            Optional<com.example.gestion_de_notes.entity.Module> moduleOpt = moduleRepository.findById(moduleId);
            if (moduleOpt.isPresent()) {
                com.example.gestion_de_notes.entity.Module module = moduleOpt.get();
                
                // Vérifier que ce module n'est pas déjà inscrit
                if (!isModuleDejaInscrit(inscription, module)) {
                    InscriptionModule inscriptionModule = new InscriptionModule();
                    inscriptionModule.setInscription(inscription);
                    inscriptionModule.setModule(module);
                    inscriptionModule.setStatut(StatutInscriptionModule.INSCRIT);
                    
                    inscriptionModuleRepository.save(inscriptionModule);
                    nbModulesAjoutes++;
                }
            }
        }
        
        // Enregistrer l'action dans le journal
        if (nbModulesAjoutes > 0) {
            journalService.enregistrerAction(utilisateur, "INSCRIPTION_MODULES_SUPPLEMENTAIRES",
                String.format("Inscription de %s à %d modules supplémentaires", 
                              etudiant.getNomComplet(), nbModulesAjoutes));
        }
        
        return true;
    }
    
    /**
     * Supprimer les inscriptions aux modules supplémentaires (niveau suivant)
     */
    private void supprimerInscriptionsModulesSupplementaires(Inscription inscription) {
        // Récupérer tous les modules du niveau actuel
        List<com.example.gestion_de_notes.entity.Module> modulesNiveauActuel = moduleRepository.findByNiveau(inscription.getNiveau());
        
        // Récupérer toutes les inscriptions aux modules de cet étudiant
        List<InscriptionModule> toutesInscriptionsModules = inscriptionModuleRepository
                .findByInscription(inscription);
        
        // Supprimer celles qui ne correspondent pas aux modules du niveau actuel
        for (InscriptionModule inscMod : toutesInscriptionsModules) {
            boolean moduleNiveauActuel = modulesNiveauActuel.stream()
                    .anyMatch(m -> m.getIdModule().equals(inscMod.getModule().getIdModule()));
            
            if (!moduleNiveauActuel) {
                // C'est un module supplémentaire, le supprimer
                inscriptionModuleRepository.delete(inscMod);
            }
        }
    }
    
    /**
     * Vérifier si un module est déjà inscrit pour cette inscription
     */
    private boolean isModuleDejaInscrit(Inscription inscription, com.example.gestion_de_notes.entity.Module module) {
        Optional<InscriptionModule> existante = inscriptionModuleRepository
                .findByInscriptionAndModule(inscription, module);
        return existante.isPresent();
    }
    
    /**
     * Récupérer les modules supplémentaires inscrits pour un étudiant
     */
    public List<com.example.gestion_de_notes.entity.Module> getModulesSupplementairesInscrits(Long idEtudiant, String anneeUniversitaire) {
        List<Inscription> inscriptions = inscriptionRepository.findByEtudiantAndAnnee(idEtudiant, anneeUniversitaire);
        if (inscriptions.isEmpty()) {
            return List.of();
        }
        
        Inscription inscription = inscriptions.get(0);
        List<com.example.gestion_de_notes.entity.Module> modulesNiveauActuel = moduleRepository.findByNiveau(inscription.getNiveau());
        
        List<InscriptionModule> toutesInscriptions = inscriptionModuleRepository
                .findByInscription(inscription);
        
        return toutesInscriptions.stream()
                .map(InscriptionModule::getModule)
                .filter(module -> modulesNiveauActuel.stream()
                        .noneMatch(m -> m.getIdModule().equals(module.getIdModule())))
                .collect(Collectors.toList());
    }
}
