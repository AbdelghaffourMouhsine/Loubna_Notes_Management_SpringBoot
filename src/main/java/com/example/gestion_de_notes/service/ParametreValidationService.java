package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.ParametreValidationDTO;
import com.example.gestion_de_notes.entity.Filiere;
import com.example.gestion_de_notes.entity.Niveau;
import com.example.gestion_de_notes.entity.ParametreValidation;
import com.example.gestion_de_notes.repository.FiliereRepository;
import com.example.gestion_de_notes.repository.NiveauRepository;
import com.example.gestion_de_notes.repository.ParametreValidationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParametreValidationService {
    
    @Autowired
    private ParametreValidationRepository parametreValidationRepository;
    
    @Autowired
    private FiliereRepository filiereRepository;
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    @Autowired
    private JournalApplicationService journalApplicationService;
    
    /**
     * Trouve tous les paramètres de validation
     */
    public List<ParametreValidationDTO> findAll() {
        return parametreValidationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Trouve un paramètre de validation par ID
     */
    public Optional<ParametreValidationDTO> findById(Long id) {
        return parametreValidationRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Trouve un paramètre de validation par filière et niveau
     */
    public Optional<ParametreValidationDTO> findByFiliereAndNiveau(Long filiereId, Long niveauId) {
        return parametreValidationRepository.findByFiliereAndNiveau(filiereId, niveauId)
                .map(this::convertToDTO);
    }
    
    /**
     * Trouve tous les paramètres de validation d'une filière
     */
    public List<ParametreValidationDTO> findByFiliereId(Long filiereId) {
        return parametreValidationRepository.findByFiliereId(filiereId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Trouve tous les paramètres de validation d'un niveau
     */
    public List<ParametreValidationDTO> findByNiveauId(Long niveauId) {
        return parametreValidationRepository.findByNiveauId(niveauId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Sauvegarde un paramètre de validation
     */
    public ParametreValidationDTO save(ParametreValidationDTO dto) {
        ParametreValidation entity;
        boolean isNew = dto.getIdParametre() == null;
        
        if (isNew) {
            entity = new ParametreValidation();
        } else {
            entity = parametreValidationRepository.findById(dto.getIdParametre())
                    .orElseThrow(() -> new RuntimeException("Paramètre de validation non trouvé"));
        }
        
        // Récupération des entités liées
        Filiere filiere = filiereRepository.findById(dto.getIdFiliere())
                .orElseThrow(() -> new RuntimeException("Filière non trouvée"));
        
        Niveau niveau = niveauRepository.findById(dto.getIdNiveau())
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));
        
        // Vérification de l'unicité
        if (isNew || !entity.getFiliere().getIdFiliere().equals(dto.getIdFiliere()) 
                || !entity.getNiveau().getIdNiveau().equals(dto.getIdNiveau())) {
            Optional<ParametreValidation> existing = parametreValidationRepository
                    .findByFiliereAndNiveau(dto.getIdFiliere(), dto.getIdNiveau());
            if (existing.isPresent() && !existing.get().getIdParametre().equals(dto.getIdParametre())) {
                throw new RuntimeException("Un paramètre de validation existe déjà pour cette combinaison filière/niveau");
            }
        }
        
        // Validation des seuils
        if (dto.getSeuilValidationRattrapage().compareTo(dto.getSeuilValidationNormale()) > 0) {
            throw new RuntimeException("Le seuil de rattrapage ne peut pas être supérieur au seuil normal");
        }
        
        // Mise à jour de l'entité
        entity.setFiliere(filiere);
        entity.setNiveau(niveau);
        entity.setSeuilValidationNormale(dto.getSeuilValidationNormale());
        entity.setSeuilValidationRattrapage(dto.getSeuilValidationRattrapage());
        
        ParametreValidation saved = parametreValidationRepository.save(entity);
        
        // Journalisation
        String action = isNew ? "Création" : "Modification";
        journalApplicationService.enregistrerAction(
            action + " paramètre validation",
            String.format("%s paramètre validation pour %s/%s - Seuils: %.2f/%.2f",
                action, filiere.getAlias(), niveau.getAlias(), 
                dto.getSeuilValidationNormale(), dto.getSeuilValidationRattrapage())
        );
        
        return convertToDTO(saved);
    }
    
    /**
     * Supprime un paramètre de validation par ID
     */
    public void deleteById(Long id) {
        ParametreValidation entity = parametreValidationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paramètre de validation non trouvé"));
        
        parametreValidationRepository.deleteById(id);
        
        // Journalisation
        journalApplicationService.enregistrerAction(
            "Suppression paramètre validation",
            String.format("Suppression paramètre validation pour %s/%s",
                entity.getFiliere().getAlias(), entity.getNiveau().getAlias())
        );
    }
    
    /**
     * Vérifie si un paramètre de validation existe pour une filière et un niveau
     */
    public boolean existsByFiliereAndNiveau(Long filiereId, Long niveauId) {
        return parametreValidationRepository.findByFiliereAndNiveau(filiereId, niveauId).isPresent();
    }
    
    /**
     * Convertit une entité en DTO
     */
    private ParametreValidationDTO convertToDTO(ParametreValidation entity) {
        ParametreValidationDTO dto = new ParametreValidationDTO();
        dto.setIdParametre(entity.getIdParametre());
        dto.setIdFiliere(entity.getFiliere().getIdFiliere());
        dto.setIdNiveau(entity.getNiveau().getIdNiveau());
        dto.setSeuilValidationNormale(entity.getSeuilValidationNormale());
        dto.setSeuilValidationRattrapage(entity.getSeuilValidationRattrapage());
        
        // Propriétés d'affichage
        dto.setFiliereAlias(entity.getFiliere().getAlias());
        dto.setFiliereIntitule(entity.getFiliere().getIntitule());
        dto.setNiveauAlias(entity.getNiveau().getAlias());
        dto.setNiveauLibelle(entity.getNiveau().getLibelle());
        
        return dto;
    }
}
