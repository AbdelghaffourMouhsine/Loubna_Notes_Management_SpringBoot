package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.EtudiantDTO;
import com.example.gestion_de_notes.dto.EtudiantSearchDTO;
import com.example.gestion_de_notes.dto.EtudiantDetailDTO;
import com.example.gestion_de_notes.entity.*;
import com.example.gestion_de_notes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@Transactional
public class EtudiantService {
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private HistoriqueModificationEtudiantRepository historiqueRepository;
    
    @Autowired
    private InscriptionRepository inscriptionRepository;
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    @Autowired
    private CompteUtilisateurRepository compteUtilisateurRepository;
    
    @Autowired
    private JournalApplicationService journalService;
    
    public List<EtudiantDTO> findAll() {
        return etudiantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<EtudiantDTO> findById(Long id) {
        return etudiantRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public Optional<EtudiantDTO> findByCne(String cne) {
        return etudiantRepository.findByCne(cne)
                .map(this::convertToDTO);
    }
    
    public EtudiantDTO save(EtudiantDTO etudiantDTO) {
        Etudiant etudiant = convertToEntity(etudiantDTO);
        Etudiant savedEtudiant = etudiantRepository.save(etudiant);
        return convertToDTO(savedEtudiant);
    }
    
    public void deleteById(Long id) {
        etudiantRepository.deleteById(id);
    }
    
    public boolean existsByCne(String cne) {
        return etudiantRepository.existsByCne(cne);
    }
    
    public List<EtudiantDTO> findByNomCompletContaining(String nomComplet) {
        return etudiantRepository.findByNomCompletContaining(nomComplet).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<EtudiantDTO> findByNiveauAndAnnee(Long niveauId, String annee) {
        return etudiantRepository.findByNiveauAndAnnee(niveauId, annee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // === NOUVELLES FONCTIONNALITÉS ===
    
    /**
     * Mise à jour d'un étudiant avec sauvegarde de l'historique
     */
    public EtudiantDTO updateEtudiant(EtudiantDTO etudiantDTO) {
        Optional<Etudiant> existingEtudiantOpt = etudiantRepository.findById(etudiantDTO.getIdEtudiant());
        if (!existingEtudiantOpt.isPresent()) {
            throw new RuntimeException("Étudiant non trouvé");
        }
        
        Etudiant existingEtudiant = existingEtudiantOpt.get();
        
        // Vérifier unicité du CNE si changé
        if (!existingEtudiant.getCne().equals(etudiantDTO.getCne()) && 
            etudiantRepository.existsByCne(etudiantDTO.getCne())) {
            throw new RuntimeException("Un étudiant avec ce CNE existe déjà");
        }
        
        // Sauvegarder l'historique des modifications
        if (hasChanges(existingEtudiant, etudiantDTO)) {
            saveHistoriqueModification(existingEtudiant, etudiantDTO);
        }
        
        // Mettre à jour l'étudiant
        existingEtudiant.setCne(etudiantDTO.getCne());
        existingEtudiant.setNom(etudiantDTO.getNom());
        existingEtudiant.setPrenom(etudiantDTO.getPrenom());
        
        Etudiant savedEtudiant = etudiantRepository.save(existingEtudiant);
        
        // Enregistrer dans le journal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Optional<CompteUtilisateur> compte = compteUtilisateurRepository.findByLogin(auth.getName());
            compte.ifPresent(c -> journalService.enregistrerAction(c, "MODIFICATION_ETUDIANT", 
                "Modification de l'étudiant: " + savedEtudiant.getCne() + " - " + savedEtudiant.getNomComplet()));
        }
        
        return convertToDTO(savedEtudiant);
    }
    
    /**
     * Recherche multi-critères d'étudiants avec utilisation du repository optimisé
     */
    public Page<EtudiantDTO> searchEtudiants(EtudiantSearchDTO searchDTO, Pageable pageable) {
        // Utiliser la méthode optimisée du repository
        List<Etudiant> etudiants = etudiantRepository.findByMultipleCriteria(
            searchDTO.getCne() != null && !searchDTO.getCne().trim().isEmpty() ? searchDTO.getCne() : null,
            searchDTO.getNom() != null && !searchDTO.getNom().trim().isEmpty() ? searchDTO.getNom() : null,
            searchDTO.getPrenom() != null && !searchDTO.getPrenom().trim().isEmpty() ? searchDTO.getPrenom() : null,
            searchDTO.getNiveauId(),
            searchDTO.getAnneeUniversitaire() != null && !searchDTO.getAnneeUniversitaire().trim().isEmpty() ? searchDTO.getAnneeUniversitaire() : null
        );
        
        // Pagination manuelle
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), etudiants.size());
        List<EtudiantDTO> paginatedResult = etudiants.subList(start, end).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new PageImpl<>(paginatedResult, pageable, etudiants.size());
    }
    
    /**
     * Obtenir les étudiants d'une classe avec détails
     */
    public List<EtudiantDetailDTO> getEtudiantsByClasse(Long niveauId, String anneeUniversitaire) {
        List<Etudiant> etudiants = etudiantRepository.findByNiveauAndAnnee(niveauId, anneeUniversitaire);
        return etudiants.stream()
            .map(this::convertToDetailDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtenir l'historique des modifications d'un étudiant
     */
    public List<HistoriqueModificationEtudiant> getHistoriqueModifications(Long etudiantId) {
        return historiqueRepository.findByEtudiantIdEtudiantOrderByDateModificationDesc(etudiantId);
    }
    
    /**
     * Modifier le niveau d'un étudiant avec contrôle de cohérence
     */
    public void modifierNiveauEtudiant(Long etudiantId, Long nouveauNiveauId, String anneeUniversitaire) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
            .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        Niveau nouveauNiveau = niveauRepository.findById(nouveauNiveauId)
            .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));
        
        // Vérifier la cohérence avec les résultats précédents
        if (!isNiveauCoherent(etudiant, nouveauNiveau)) {
            throw new RuntimeException("Le niveau choisi n'est pas cohérent avec les résultats de l'étudiant");
        }
        
        // Créer nouvelle inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setNiveau(nouveauNiveau);
        inscription.setAnneeUniversitaire(anneeUniversitaire);
        inscription.setTypeInscription(TypeInscription.REINSCRIPTION);
        
        inscriptionRepository.save(inscription);
        
        // Enregistrer dans le journal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Optional<CompteUtilisateur> compte = compteUtilisateurRepository.findByLogin(auth.getName());
            compte.ifPresent(c -> journalService.enregistrerAction(c, "MODIFICATION_NIVEAU_ETUDIANT", 
                "Modification niveau étudiant: " + etudiant.getCne() + " vers " + nouveauNiveau.getAlias()));
        }
    }
    
    /**
     * Supprimer un étudiant avec contrôles
     */
    public void deleteEtudiantAvecControles(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        // Vérifier s'il a des notes
        if (!etudiant.getNotes().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un étudiant qui a des notes");
        }
        
        // Enregistrer dans le journal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Optional<CompteUtilisateur> compte = compteUtilisateurRepository.findByLogin(auth.getName());
            compte.ifPresent(c -> journalService.enregistrerAction(c, "SUPPRESSION_ETUDIANT", 
                "Suppression de l'étudiant: " + etudiant.getCne() + " - " + etudiant.getNomComplet()));
        }
        
        etudiantRepository.deleteById(id);
    }
    
    // === MÉTHODES PRIVÉES ===
    
    private boolean hasChanges(Etudiant existing, EtudiantDTO dto) {
        return !existing.getCne().equals(dto.getCne()) ||
               !existing.getNom().equals(dto.getNom()) ||
               !existing.getPrenom().equals(dto.getPrenom());
    }
    
    private void saveHistoriqueModification(Etudiant existing, EtudiantDTO dto) {
        HistoriqueModificationEtudiant historique = new HistoriqueModificationEtudiant();
        historique.setEtudiant(existing);
        historique.setAncienCne(existing.getCne());
        historique.setAncienNom(existing.getNom());
        historique.setAncienPrenom(existing.getPrenom());
        historique.setNouveauCne(dto.getCne());
        historique.setNouveauNom(dto.getNom());
        historique.setNouveauPrenom(dto.getPrenom());
        
        // Obtenir l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Optional<CompteUtilisateur> compte = compteUtilisateurRepository.findByLogin(auth.getName());
            compte.ifPresent(historique::setUtilisateurModification);
        }
        
        historiqueRepository.save(historique);
    }
    
    private boolean isNiveauCoherent(Etudiant etudiant, Niveau nouveauNiveau) {
        // Logique simplifiée - À enrichir selon les règles métier
        // Vérifier que l'étudiant n'a pas déjà validé un niveau supérieur
        return true; // Pour l'instant, on autorise toutes les modifications
    }
    
    private EtudiantDTO convertToDTO(Etudiant etudiant) {
        EtudiantDTO dto = new EtudiantDTO();
        dto.setIdEtudiant(etudiant.getIdEtudiant());
        dto.setCne(etudiant.getCne());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setNomComplet(etudiant.getNomComplet());
        return dto;
    }
    
    private EtudiantDetailDTO convertToDetailDTO(Etudiant etudiant) {
        EtudiantDetailDTO dto = new EtudiantDetailDTO();
        dto.setIdEtudiant(etudiant.getIdEtudiant());
        dto.setCne(etudiant.getCne());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setNomComplet(etudiant.getNomComplet());
        
        // Ajouter les informations d'inscription actuelles
        if (!etudiant.getInscriptions().isEmpty()) {
            Inscription inscriptionActuelle = etudiant.getInscriptions().stream()
                .reduce((i1, i2) -> i1.getDateInscription().isAfter(i2.getDateInscription()) ? i1 : i2)
                .orElse(null);
            
            if (inscriptionActuelle != null) {
                dto.setNiveauActuel(inscriptionActuelle.getNiveau().getAlias());
                dto.setAnneeUniversitaire(inscriptionActuelle.getAnneeUniversitaire());
                dto.setTypeInscription(inscriptionActuelle.getTypeInscription().name());
            }
        }
        
        return dto;
    }
    
    private Etudiant convertToEntity(EtudiantDTO dto) {
        Etudiant etudiant = new Etudiant();
        etudiant.setIdEtudiant(dto.getIdEtudiant());
        etudiant.setCne(dto.getCne());
        etudiant.setNom(dto.getNom());
        etudiant.setPrenom(dto.getPrenom());
        return etudiant;
    }
}
