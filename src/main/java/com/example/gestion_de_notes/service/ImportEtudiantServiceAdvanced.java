package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.*;
import com.example.gestion_de_notes.entity.*;
import com.example.gestion_de_notes.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImportEtudiantServiceAdvanced {
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private InscriptionRepository inscriptionRepository;
    
    @Autowired
    private InscriptionModuleRepository inscriptionModuleRepository;
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private JournalApplicationService journalService;
    
    @Autowired
    private HistoriqueModificationEtudiantService historiqueService;
    
    // Map pour stocker temporairement les données de fichier pendant la session
    private static final Map<String, List<EtudiantImportData>> tempDataStorage = new HashMap<>();
    
    /**
     * Première étape : Validation du fichier et détection des conflits
     */
    public ValidationImportDTO validerFichier(MultipartFile file, String anneeUniversitaire) {
        ValidationImportDTO validation = new ValidationImportDTO();
        validation.setAnneeUniversitaire(anneeUniversitaire);
        validation.setFileName(file.getOriginalFilename());
        
        try {
            // Vérification du format de fichier
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                validation.setSuccess(false);
                validation.setMessage("Format de fichier incorrect. Seuls les fichiers Excel (.xlsx) sont acceptés");
                return validation;
            }
            
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            // Vérification de la structure du fichier
            if (!verifierStructureFichier(sheet, validation)) {
                return validation;
            }
            
            List<EtudiantImportData> etudiants = lireDonneesFichier(sheet, validation);
            if (!validation.isSuccess()) {
                return validation;
            }
            
            // Validation des données et détection des conflits
            validerDonneesEtDetecterConflits(etudiants, validation);
            
            // Stocker les données pour la suite du processus
            String sessionId = UUID.randomUUID().toString();
            tempDataStorage.put(sessionId, etudiants);
            validation.setSessionId(sessionId);
            
            workbook.close();
            
            if (validation.getErrors().isEmpty()) {
                validation.setSuccess(true);
                if (validation.hasConflits()) {
                    validation.setMessage("Validation réussie avec des conflits de données détectés");
                } else {
                    validation.setMessage("Validation réussie, aucun conflit détecté");
                }
            }
            
        } catch (IOException e) {
            validation.setSuccess(false);
            validation.setMessage("Erreur lors de la lecture du fichier: " + e.getMessage());
        }
        
        return validation;
    }
    
    /**
     * Deuxième étape : Import avec résolution des conflits
     */
    public ImportEtudiantService.ImportResult importerAvecResolutionConflits(
            String sessionId, List<ConflitDonneesDTO> conflitsResolus, 
            CompteUtilisateur utilisateur, String anneeUniversitaire) {
        
        ImportEtudiantService.ImportResult result = new ImportEtudiantService.ImportResult();
        
        List<EtudiantImportData> etudiants = tempDataStorage.get(sessionId);
        if (etudiants == null) {
            result.setSuccess(false);
            result.setMessage("Session expirée. Veuillez recommencer l'import");
            return result;
        }
        
        try {
            // Appliquer les résolutions de conflits
            appliquerResolutionConflits(etudiants, conflitsResolus);
            
            // Traitement des étudiants
            traiterEtudiants(etudiants, anneeUniversitaire, utilisateur, result);
            
            // Nettoyer les données temporaires
            tempDataStorage.remove(sessionId);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Erreur lors de l'import: " + e.getMessage());
        }
        
        return result;
    }
    
    private boolean verifierStructureFichier(Sheet sheet, ValidationImportDTO validation) {        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            validation.setSuccess(false);
            validation.setMessage("Le fichier ne contient pas d'en-tête");
            return false;
        }
        
        // Vérifier les colonnes attendues: ID_ETUDIANT, CNE, NOM, PRENOM, ID_NIVEAU_ACTUEL, TYPE
        String[] expectedHeaders = {"ID_ETUDIANT", "CNE", "NOM", "PRENOM", "ID_NIVEAU_ACTUEL", "TYPE"};
        
        if (headerRow.getLastCellNum() < expectedHeaders.length) {
            validation.setSuccess(false);
            validation.setMessage("Le fichier ne contient pas toutes les colonnes requises");
            return false;
        }
        
        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !expectedHeaders[i].equals(cell.getStringCellValue().trim())) {
                validation.setSuccess(false);
                validation.setMessage("En-tête incorrect à la colonne " + (i + 1) + ". Attendu: " + expectedHeaders[i]);
                return false;
            }
        }
        
        return true;
    }
    
    private static class EtudiantImportData {
        public String idEtudiant;
        public String cne;
        public String nom;
        public String prenom;
        public Long idNiveau;
        public TypeInscription type;
        public int rowNumber;
        public boolean resolutionConflit = false;
        public boolean mettreAJour = false;
    }
    
    private List<EtudiantImportData> lireDonneesFichier(Sheet sheet, ValidationImportDTO validation) {
        List<EtudiantImportData> etudiants = new ArrayList<>();
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            try {
                EtudiantImportData data = new EtudiantImportData();
                data.rowNumber = i + 1;
                
                // Lecture des cellules
                data.idEtudiant = getCellStringValue(row.getCell(0));
                data.cne = getCellStringValue(row.getCell(1));
                data.nom = getCellStringValue(row.getCell(2));
                data.prenom = getCellStringValue(row.getCell(3));
                
                String idNiveauStr = getCellStringValue(row.getCell(4));
                if (idNiveauStr != null && !idNiveauStr.trim().isEmpty()) {
                    try {
                        data.idNiveau = Long.parseLong(idNiveauStr.trim());
                    } catch (NumberFormatException e) {
                        validation.getErrors().add("Ligne " + data.rowNumber + ": ID niveau invalide");
                        continue;
                    }
                }
                
                String typeStr = getCellStringValue(row.getCell(5));
                if ("INSCRIPTION".equalsIgnoreCase(typeStr)) {
                    data.type = TypeInscription.INSCRIPTION;
                } else if ("REINSCRIPTION".equalsIgnoreCase(typeStr)) {
                    data.type = TypeInscription.REINSCRIPTION;
                } else {
                    validation.getErrors().add("Ligne " + data.rowNumber + ": Type invalide (doit être INSCRIPTION ou REINSCRIPTION)");
                    continue;
                }
                
                // Validation des données obligatoires
                if (data.cne == null || data.cne.trim().isEmpty()) {
                    validation.getErrors().add("Ligne " + data.rowNumber + ": CNE manquant");
                    continue;
                }
                if (data.nom == null || data.nom.trim().isEmpty()) {
                    validation.getErrors().add("Ligne " + data.rowNumber + ": Nom manquant");
                    continue;
                }
                if (data.prenom == null || data.prenom.trim().isEmpty()) {
                    validation.getErrors().add("Ligne " + data.rowNumber + ": Prénom manquant");
                    continue;
                }
                if (data.idNiveau == null) {
                    validation.getErrors().add("Ligne " + data.rowNumber + ": ID niveau manquant");
                    continue;
                }
                
                etudiants.add(data);
                
            } catch (Exception e) {
                validation.getErrors().add("Ligne " + (i + 1) + ": Erreur de lecture - " + e.getMessage());
            }
        }
        
        if (!validation.getErrors().isEmpty()) {
            validation.setSuccess(false);
            validation.setMessage("Erreurs de validation détectées dans le fichier");
        }
        
        return etudiants;
    }
    
    private void validerDonneesEtDetecterConflits(List<EtudiantImportData> etudiants, ValidationImportDTO validation) {
        for (EtudiantImportData data : etudiants) {
            try {
                // a) Vérifier l'existence du niveau
                Optional<Niveau> niveauOpt = niveauRepository.findById(data.idNiveau);
                if (!niveauOpt.isPresent()) {
                    validation.getErrors().add("Ligne " + data.rowNumber + ": Niveau avec ID " + data.idNiveau + " introuvable");
                    continue;
                }
                
                Niveau niveau = niveauOpt.get();
                
                if (data.type == TypeInscription.INSCRIPTION) {
                    // b) Vérifier que l'étudiant n'existe pas déjà
                    if (etudiantRepository.existsByCne(data.cne)) {
                        validation.getErrors().add("Ligne " + data.rowNumber + ": Un étudiant avec le CNE " + data.cne + " existe déjà");
                        continue;
                    }
                    
                } else { // REINSCRIPTION
                    // c) Vérifier que l'étudiant existe
                    Optional<Etudiant> etudiantOpt = etudiantRepository.findByCne(data.cne);
                    if (!etudiantOpt.isPresent()) {
                        validation.getErrors().add("Ligne " + data.rowNumber + ": Étudiant avec CNE " + data.cne + " introuvable pour la réinscription");
                        continue;
                    }
                    
                    Etudiant etudiant = etudiantOpt.get();
                    
                    // d) Vérifier la cohérence du niveau avec les résultats passés
                    if (!verifierCoherenceNiveau(etudiant, niveau)) {
                        validation.getErrors().add("Ligne " + data.rowNumber + ": Le niveau " + niveau.getAlias() + 
                            " n'est pas cohérent avec les résultats de l'étudiant " + data.cne);
                        continue;
                    }
                    
                    // Détecter les conflits de données
                    if (!etudiant.getNom().equals(data.nom) || !etudiant.getPrenom().equals(data.prenom)) {
                        ConflitDonneesDTO conflit = new ConflitDonneesDTO();
                        conflit.setCne(data.cne);
                        conflit.setNomFichier(data.nom);
                        conflit.setPrenomFichier(data.prenom);
                        conflit.setNomBase(etudiant.getNom());
                        conflit.setPrenomBase(etudiant.getPrenom());
                        conflit.setNumeroLigne(data.rowNumber);
                        conflit.setMettreAJour(false); // Par défaut, ne pas mettre à jour
                        
                        validation.getConflits().add(conflit);
                    }
                }
                
            } catch (Exception e) {
                validation.getErrors().add("Ligne " + data.rowNumber + ": Erreur de validation - " + e.getMessage());
            }
        }
    }
    
    private boolean verifierCoherenceNiveau(Etudiant etudiant, Niveau niveauDemande) {
        // Récupérer la dernière inscription de l'étudiant
        List<Inscription> inscriptions = inscriptionRepository.findByEtudiantCne(etudiant.getCne());
        if (inscriptions.isEmpty()) {
            return true; // Premier niveau, pas de contrainte
        }
        
        // Trier par date d'inscription pour avoir la plus récente
        inscriptions.sort((i1, i2) -> i2.getDateInscription().compareTo(i1.getDateInscription()));
        Inscription derniereInscription = inscriptions.get(0);
        
        // Vérifier si l'étudiant a validé son niveau précédent
        boolean niveauValide = verifierValidationNiveau(etudiant, derniereInscription.getNiveau());
        
        if (niveauValide) {
            // Si niveau validé, le nouveau niveau doit être le niveau suivant
            return derniereInscription.getNiveau().getNiveauSuivant() != null && 
                   derniereInscription.getNiveau().getNiveauSuivant().getIdNiveau().equals(niveauDemande.getIdNiveau());
        } else {
            // Si niveau non validé, peut reprendre le même niveau ou niveau suivant (étudiant ajourné)
            return derniereInscription.getNiveau().getIdNiveau().equals(niveauDemande.getIdNiveau()) ||
                   (derniereInscription.getNiveau().getNiveauSuivant() != null &&
                    derniereInscription.getNiveau().getNiveauSuivant().getIdNiveau().equals(niveauDemande.getIdNiveau()));
        }
    }
    
    private boolean verifierValidationNiveau(Etudiant etudiant, Niveau niveau) {
        // Récupérer tous les modules du niveau
        List<com.example.gestion_de_notes.entity.Module> modules = moduleRepository.findByNiveau(niveau);
        
        for (com.example.gestion_de_notes.entity.Module module : modules) {
            // Vérifier si l'étudiant a une note validée pour ce module
            Optional<Note> noteOpt = noteRepository.findByEtudiantAndModule(etudiant.getIdEtudiant(), module.getIdModule());
            if (!noteOpt.isPresent()) {
                return false; // Aucune note trouvée
            }
            
            Note note = noteOpt.get();
            // TODO: Implémenter la logique de validation basée sur les seuils
            // Pour l'instant, on considère que si une note existe, le module est validé
            // Cette logique doit être adaptée selon les règles métier
        }
        
        return true; // Tous les modules sont validés
    }
    
    private void appliquerResolutionConflits(List<EtudiantImportData> etudiants, List<ConflitDonneesDTO> conflitsResolus) {
        Map<String, Boolean> resolutions = conflitsResolus.stream()
            .collect(Collectors.toMap(ConflitDonneesDTO::getCne, ConflitDonneesDTO::isMettreAJour));
        
        for (EtudiantImportData data : etudiants) {
            if (resolutions.containsKey(data.cne)) {
                data.resolutionConflit = true;
                data.mettreAJour = resolutions.get(data.cne);
            }
        }
    }
    
    private void traiterEtudiants(List<EtudiantImportData> etudiants, String anneeUniversitaire, 
                                 CompteUtilisateur utilisateur, ImportEtudiantService.ImportResult result) {
        
        for (EtudiantImportData data : etudiants) {
            try {
                Optional<Niveau> niveauOpt = niveauRepository.findById(data.idNiveau);
                if (!niveauOpt.isPresent()) {
                    result.getErrors().add("Ligne " + data.rowNumber + ": Niveau introuvable");
                    continue;
                }
                
                Niveau niveau = niveauOpt.get();
                
                if (data.type == TypeInscription.INSCRIPTION) {
                    traiterNouvelEtudiant(data, niveau, anneeUniversitaire, utilisateur, result);
                } else {
                    traiterReinscriptionEtudiant(data, niveau, anneeUniversitaire, utilisateur, result);
                }
                
            } catch (Exception e) {
                result.getErrors().add("Ligne " + data.rowNumber + ": Erreur de traitement - " + e.getMessage());
            }
        }
        
        if (result.getErrors().isEmpty()) {
            result.setSuccess(true);
            result.setMessage("Import terminé avec succès");
        } else {
            result.setSuccess(false);
            result.setMessage("Import terminé avec des erreurs");
        }
    }
    
    private void traiterNouvelEtudiant(EtudiantImportData data, Niveau niveau, String anneeUniversitaire,
                                      CompteUtilisateur utilisateur, ImportEtudiantService.ImportResult result) {
        
        // Créer le nouvel étudiant
        Etudiant etudiant = new Etudiant();
        etudiant.setCne(data.cne);
        etudiant.setNom(data.nom);
        etudiant.setPrenom(data.prenom);
        etudiant = etudiantRepository.save(etudiant);
        
        // Créer l'inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setNiveau(niveau);
        inscription.setAnneeUniversitaire(anneeUniversitaire);
        inscription.setTypeInscription(TypeInscription.INSCRIPTION);
        inscription = inscriptionRepository.save(inscription);
        
        // Inscrire dans tous les modules du niveau
        inscrireDansTousLesModules(etudiant, inscription, niveau);
        
        result.setNbInscriptions(result.getNbInscriptions() + 1);
        
        // Enregistrer dans le journal
        journalService.enregistrerAction(utilisateur, "INSCRIPTION_ETUDIANT", 
            "Inscription de l'étudiant " + etudiant.getNomComplet() + " (CNE: " + etudiant.getCne() + ") en " + niveau.getAlias());
    }
    
    private void traiterReinscriptionEtudiant(EtudiantImportData data, Niveau niveau, String anneeUniversitaire,
                                             CompteUtilisateur utilisateur, ImportEtudiantService.ImportResult result) {
        
        Optional<Etudiant> etudiantOpt = etudiantRepository.findByCne(data.cne);
        if (!etudiantOpt.isPresent()) {
            result.getErrors().add("Ligne " + data.rowNumber + ": Étudiant introuvable");
            return;
        }
        
        Etudiant etudiant = etudiantOpt.get();
        
        // Gérer la résolution de conflit si nécessaire
        if (data.resolutionConflit && data.mettreAJour) {
            // Enregistrer l'historique avant modification
            historiqueService.enregistrerModification(etudiant, 
                etudiant.getCne(), etudiant.getNom(), etudiant.getPrenom(),
                data.cne, data.nom, data.prenom, utilisateur);
            
            // Mettre à jour les données
            etudiant.setNom(data.nom);
            etudiant.setPrenom(data.prenom);
            etudiant = etudiantRepository.save(etudiant);
            
            result.getWarnings().add("Ligne " + data.rowNumber + ": Données de l'étudiant " + data.cne + " mises à jour");
        }
        
        // Vérifier si l'inscription existe déjà
        Optional<Inscription> inscriptionExistante = inscriptionRepository
            .findByEtudiantNiveauAndAnnee(etudiant.getIdEtudiant(), niveau.getIdNiveau(), anneeUniversitaire);
        
        if (inscriptionExistante.isPresent()) {
            result.getWarnings().add("Ligne " + data.rowNumber + ": L'étudiant " + data.cne + " est déjà inscrit en " + niveau.getAlias());
            return;
        }
        
        // Créer la nouvelle inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setNiveau(niveau);
        inscription.setAnneeUniversitaire(anneeUniversitaire);
        inscription.setTypeInscription(TypeInscription.REINSCRIPTION);
        inscription = inscriptionRepository.save(inscription);
        
        // Gérer l'inscription dans les modules selon le statut de l'étudiant
        gererInscriptionModules(etudiant, inscription, niveau, anneeUniversitaire);
        
        result.setNbReinscriptions(result.getNbReinscriptions() + 1);
        
        // Enregistrer dans le journal
        journalService.enregistrerAction(utilisateur, "REINSCRIPTION_ETUDIANT", 
            "Réinscription de l'étudiant " + etudiant.getNomComplet() + " (CNE: " + etudiant.getCne() + ") en " + niveau.getAlias());
    }
    
    private void gererInscriptionModules(Etudiant etudiant, Inscription inscription, Niveau niveau, String anneeUniversitaire) {
        // Vérifier si l'étudiant est ajourné (a des modules non validés du niveau précédent)
        List<com.example.gestion_de_notes.entity.Module> modulesNonValides = getModulesNonValides(etudiant, niveau);
        
        if (modulesNonValides.isEmpty()) {
            // Étudiant non ajourné : inscription dans tous les modules du niveau
            inscrireDansTousLesModules(etudiant, inscription, niveau);
        } else {
            // Étudiant ajourné : inscription uniquement dans les modules non validés
            inscrireDansModulesNonValides(etudiant, inscription, modulesNonValides);
        }
    }
    
    private List<com.example.gestion_de_notes.entity.Module> getModulesNonValides(Etudiant etudiant, Niveau niveau) {
        List<com.example.gestion_de_notes.entity.Module> modulesNonValides = new ArrayList<>();
        
        // Récupérer les inscriptions précédentes de l'étudiant
        List<Inscription> inscriptionsPrecedentes = inscriptionRepository.findByEtudiantCne(etudiant.getCne());
        
        for (Inscription inscPrec : inscriptionsPrecedentes) {
            List<com.example.gestion_de_notes.entity.Module> modules = moduleRepository.findByNiveau(inscPrec.getNiveau());
            
            for (com.example.gestion_de_notes.entity.Module module : modules) {
                Optional<Note> noteOpt = noteRepository.findByEtudiantAndModule(etudiant.getIdEtudiant(), module.getIdModule());
                
                if (!noteOpt.isPresent()) {
                    // Si aucune note n'existe, le module n'est pas validé
                    modulesNonValides.add(module);
                } else {
                    // TODO: Implémenter la logique de validation basée sur les seuils
                    // Pour l'instant, on considère que si une note existe, le module est validé
                    // Cette logique doit être adaptée selon les règles métier
                }
            }
        }
        
        return modulesNonValides;
    }
    
    private void inscrireDansTousLesModules(Etudiant etudiant, Inscription inscription, Niveau niveau) {
        List<com.example.gestion_de_notes.entity.Module> modules = moduleRepository.findByNiveau(niveau);
        
        for (com.example.gestion_de_notes.entity.Module module : modules) {
            InscriptionModule inscriptionModule = new InscriptionModule();
            inscriptionModule.setInscription(inscription);
            inscriptionModule.setModule(module);
            inscriptionModule.setStatut(StatutInscriptionModule.INSCRIT);
            inscriptionModuleRepository.save(inscriptionModule);
        }
    }
    
    private void inscrireDansModulesNonValides(Etudiant etudiant, Inscription inscription, List<com.example.gestion_de_notes.entity.Module> modulesNonValides) {
        for (com.example.gestion_de_notes.entity.Module module : modulesNonValides) {
            InscriptionModule inscriptionModule = new InscriptionModule();
            inscriptionModule.setInscription(inscription);
            inscriptionModule.setModule(module);
            inscriptionModule.setStatut(StatutInscriptionModule.INSCRIT);
            inscriptionModuleRepository.save(inscriptionModule);
        }
    }
    
    /**
     * Méthode pour permettre l'inscription d'étudiants ajournés dans des modules du niveau suivant
     */
    public List<EtudiantAjourneDTO> getEtudiantsAjournes(String anneeUniversitaire) {
        List<EtudiantAjourneDTO> etudiantsAjournes = new ArrayList<>();
        
        // Récupérer tous les étudiants qui ont des modules non validés
        List<Inscription> inscriptions = inscriptionRepository.findByTypeInscriptionAndAnneeUniversitaire(
            TypeInscription.REINSCRIPTION, anneeUniversitaire);
        
        for (Inscription inscription : inscriptions) {
            Etudiant etudiant = inscription.getEtudiant();
            Niveau niveauActuel = inscription.getNiveau();
            
            List<com.example.gestion_de_notes.entity.Module> modulesNonValides = getModulesNonValides(etudiant, niveauActuel);
            
            if (!modulesNonValides.isEmpty()) {
                EtudiantAjourneDTO dto = new EtudiantAjourneDTO();
                dto.setIdEtudiant(etudiant.getIdEtudiant());
                dto.setCne(etudiant.getCne());
                dto.setNomComplet(etudiant.getNomComplet());
                dto.setNiveauActuel(niveauActuel.getIdNiveau());
                dto.setLibelleNiveauActuel(niveauActuel.getLibelle());
                
                if (niveauActuel.getNiveauSuivant() != null) {
                    dto.setNiveauSuivant(niveauActuel.getNiveauSuivant().getIdNiveau());
                    dto.setLibelleNiveauSuivant(niveauActuel.getNiveauSuivant().getLibelle());
                    
                    // Récupérer les modules du niveau suivant
                    List<com.example.gestion_de_notes.entity.Module> modulesSuivants = moduleRepository.findByNiveau(niveauActuel.getNiveauSuivant());
                    dto.setModulesNiveauSuivant(modulesSuivants.stream()
                        .map(this::convertToModuleDTO)
                        .collect(Collectors.toList()));
                }
                
                dto.setModulesNonValides(modulesNonValides.stream()
                    .map(this::convertToModuleDTO)
                    .collect(Collectors.toList()));
                
                etudiantsAjournes.add(dto);
            }
        }
        
        return etudiantsAjournes;
    }
    
    private ModuleDTO convertToModuleDTO(com.example.gestion_de_notes.entity.Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setIdModule(module.getIdModule());
        dto.setTitre(module.getTitre());
        dto.setCode(module.getCode());
        return dto;
    }
    
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}