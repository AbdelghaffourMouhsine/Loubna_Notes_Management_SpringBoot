package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.EtudiantDTO;
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

@Service
@Transactional
public class ImportEtudiantService {
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private InscriptionRepository inscriptionRepository;
    
    @Autowired
    private InscriptionModuleRepository inscriptionModuleRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    @Autowired
    private JournalApplicationService journalService;
    
    @Autowired
    private HistoriqueModificationEtudiantService historiqueService;
    
    public static class ImportResult {
        private boolean success;
        private String message;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private int nbInscriptions = 0;
        private int nbReinscriptions = 0;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        public int getNbInscriptions() { return nbInscriptions; }
        public void setNbInscriptions(int nbInscriptions) { this.nbInscriptions = nbInscriptions; }
        public int getNbReinscriptions() { return nbReinscriptions; }
        public void setNbReinscriptions(int nbReinscriptions) { this.nbReinscriptions = nbReinscriptions; }
    }
    
    /**
     * Inscrit automatiquement un étudiant à tous les modules d'un niveau
     */
    private void inscrireAuxModulesDuNiveau(Inscription inscription, Niveau niveau) {
        System.out.println("DEBUG: Début inscription aux modules pour niveau: " + niveau.getAlias());
        
        // Récupérer tous les modules de ce niveau
        List<com.example.gestion_de_notes.entity.Module> modules = moduleRepository.findByNiveau(niveau);
        System.out.println("DEBUG: Nombre de modules trouvés pour le niveau " + niveau.getAlias() + ": " + modules.size());
        
        for (com.example.gestion_de_notes.entity.Module module : modules) {
            try {
                // Vérifier si l'inscription au module n'existe pas déjà
                Optional<InscriptionModule> existante = inscriptionModuleRepository
                    .findByInscriptionAndModule(inscription, module);
                
                if (!existante.isPresent()) {
                    // Créer la nouvelle inscription au module
                    InscriptionModule inscriptionModule = new InscriptionModule();
                    inscriptionModule.setInscription(inscription);
                    inscriptionModule.setModule(module);
                    inscriptionModule.setStatut(StatutInscriptionModule.INSCRIT);
                    
                    inscriptionModuleRepository.save(inscriptionModule);
                    System.out.println("DEBUG: Inscription créée pour module: " + module.getTitre());
                } else {
                    System.out.println("DEBUG: Inscription déjà existante pour module: " + module.getTitre());
                }
                
            } catch (Exception e) {
                System.out.println("DEBUG: Erreur lors de l'inscription au module " + module.getTitre() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("DEBUG: Fin inscription aux modules pour niveau: " + niveau.getAlias());
    }
    
    public ImportResult importerEtudiants(MultipartFile file, String anneeUniversitaire, 
                                         CompteUtilisateur utilisateur) {
        ImportResult result = new ImportResult();
        
        System.out.println("DEBUG: Import démarré avec fichier: " + file.getOriginalFilename());
        System.out.println("DEBUG: Année universitaire: " + anneeUniversitaire);
        System.out.println("DEBUG: Utilisateur: " + (utilisateur != null ? utilisateur.getLogin() : "null"));
        
        try {
            // Vérification du format de fichier
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                result.setSuccess(false);
                result.setMessage("Format de fichier incorrect. Seuls les fichiers Excel (.xlsx) sont acceptés");
                return result;
            }
            
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            // Vérification de la structure du fichier
            if (!verifierStructureFichier(sheet, result)) {
                return result;
            }
            
            List<EtudiantImportData> etudiants = lireDonneesFichier(sheet, result);
            System.out.println("DEBUG: Nombre d'étudiants lus: " + etudiants.size());
            if (!result.isSuccess()) {
                System.out.println("DEBUG: Erreur lors de la lecture: " + result.getMessage());
                return result;
            }
            
            // Traitement des étudiants
            System.out.println("DEBUG: Début du traitement des étudiants");
            traiterEtudiants(etudiants, anneeUniversitaire, utilisateur, result);
            System.out.println("DEBUG: Fin du traitement - Success: " + result.isSuccess() + ", Message: " + result.getMessage());
            
            workbook.close();
            
        } catch (IOException e) {
            System.out.println("DEBUG: Exception IOException: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("Erreur lors de la lecture du fichier: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("DEBUG: Exception générale: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("Erreur inattendue: " + e.getMessage());
        }
        
        return result;
    }
    
    private boolean verifierStructureFichier(Sheet sheet, ImportResult result) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            result.setSuccess(false);
            result.setMessage("Le fichier ne contient pas d'en-tête");
            return false;
        }
        
        // Vérifier les colonnes attendues: ID_ETUDIANT, CNE, NOM, PRENOM, ID_NIVEAU_ACTUEL, TYPE
        String[] expectedHeaders = {"ID_ETUDIANT", "CNE", "NOM", "PRENOM", "ID_NIVEAU_ACTUEL", "TYPE"};
        
        if (headerRow.getLastCellNum() < expectedHeaders.length) {
            result.setSuccess(false);
            result.setMessage("Le fichier ne contient pas toutes les colonnes requises");
            return false;
        }
        
        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !expectedHeaders[i].equals(cell.getStringCellValue().trim())) {
                result.setSuccess(false);
                result.setMessage("En-tête incorrect à la colonne " + (i + 1) + ". Attendu: " + expectedHeaders[i]);
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
    }
    
    private List<EtudiantImportData> lireDonneesFichier(Sheet sheet, ImportResult result) {
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
                        result.getErrors().add("Ligne " + data.rowNumber + ": ID niveau invalide");
                        continue;
                    }
                }
                
                String typeStr = getCellStringValue(row.getCell(5));
                if ("INSCRIPTION".equalsIgnoreCase(typeStr)) {
                    data.type = TypeInscription.INSCRIPTION;
                } else if ("REINSCRIPTION".equalsIgnoreCase(typeStr)) {
                    data.type = TypeInscription.REINSCRIPTION;
                } else {
                    result.getErrors().add("Ligne " + data.rowNumber + ": Type invalide (doit être INSCRIPTION ou REINSCRIPTION)");
                    continue;
                }
                
                // Validation des données
                System.out.println("DEBUG: Validation pour ligne " + data.rowNumber + " - CNE: " + data.cne + ", Nom: " + data.nom + ", Prenom: " + data.prenom + ", IdNiveau: " + data.idNiveau);
                
                if (data.cne == null || data.cne.trim().isEmpty()) {
                    System.out.println("DEBUG: CNE manquant pour ligne " + data.rowNumber);
                    result.getErrors().add("Ligne " + data.rowNumber + ": CNE manquant");
                    continue;
                }
                if (data.nom == null || data.nom.trim().isEmpty()) {
                    System.out.println("DEBUG: Nom manquant pour ligne " + data.rowNumber);
                    result.getErrors().add("Ligne " + data.rowNumber + ": Nom manquant");
                    continue;
                }
                if (data.prenom == null || data.prenom.trim().isEmpty()) {
                    System.out.println("DEBUG: Prénom manquant pour ligne " + data.rowNumber);
                    result.getErrors().add("Ligne " + data.rowNumber + ": Prénom manquant");
                    continue;
                }
                if (data.idNiveau == null) {
                    System.out.println("DEBUG: ID niveau manquant pour ligne " + data.rowNumber);
                    result.getErrors().add("Ligne " + data.rowNumber + ": ID niveau manquant");
                    continue;
                }
                
                System.out.println("DEBUG: Étudiant valide ajouté - ligne " + data.rowNumber);
                
                etudiants.add(data);
                
            } catch (Exception e) {
                System.out.println("DEBUG: Exception lors de la lecture ligne " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
                result.getErrors().add("Ligne " + (i + 1) + ": Erreur de lecture - " + e.getMessage());
            }
        }
        
        if (!result.getErrors().isEmpty()) {
            System.out.println("DEBUG: Erreurs détectées: " + result.getErrors().size());
            for (String error : result.getErrors()) {
                System.out.println("DEBUG: Erreur: " + error);
            }
            result.setSuccess(false);
            result.setMessage("Erreurs de validation détectées dans le fichier");
            System.out.println("DEBUG: Message d'erreur défini: " + result.getMessage());
        } else {
            System.out.println("DEBUG: Aucune erreur détectée, marquage comme succès");
            result.setSuccess(true);
        }
        
        return etudiants;
    }
    
    private void traiterEtudiants(List<EtudiantImportData> etudiants, String anneeUniversitaire, 
                                 CompteUtilisateur utilisateur, ImportResult result) {
        
        for (EtudiantImportData data : etudiants) {
            try {
                // Vérifier l'existence du niveau
                Optional<Niveau> niveauOpt = niveauRepository.findById(data.idNiveau);
                if (!niveauOpt.isPresent()) {
                    result.getErrors().add("Ligne " + data.rowNumber + ": Niveau avec ID " + data.idNiveau + " introuvable");
                    continue;
                }
                
                Niveau niveau = niveauOpt.get();
                
                if (data.type == TypeInscription.INSCRIPTION) {
                    System.out.println("DEBUG: Traitement inscription - CNE: " + data.cne);
                    // Nouvel étudiant
                    if (etudiantRepository.existsByCne(data.cne)) {
                        result.getErrors().add("Ligne " + data.rowNumber + ": Un étudiant avec le CNE " + data.cne + " existe déjà");
                        continue;
                    }
                    
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
                    System.out.println("DEBUG: Inscription sauvegardée pour: " + data.cne);
                    
                    // Inscrire automatiquement dans tous les modules du niveau
                    inscrireAuxModulesDuNiveau(inscription, niveau);
                    System.out.println("DEBUG: Inscription aux modules terminée pour: " + data.cne);
                    
                    result.setNbInscriptions(result.getNbInscriptions() + 1);
                    
                    // Enregistrer dans le journal
                    journalService.enregistrerAction(utilisateur, "INSCRIPTION_ETUDIANT", 
                        "Inscription de l'étudiant " + etudiant.getNomComplet() + " (CNE: " + etudiant.getCne() + ") en " + niveau.getAlias());
                    
                } else {
                    System.out.println("DEBUG: Traitement réinscription - CNE: " + data.cne);
                    // Réinscription
                    Optional<Etudiant> etudiantOpt = etudiantRepository.findByCne(data.cne);
                    if (!etudiantOpt.isPresent()) {
                        result.getErrors().add("Ligne " + data.rowNumber + ": Étudiant avec CNE " + data.cne + " introuvable pour la réinscription");
                        continue;
                    }
                    
                    Etudiant etudiant = etudiantOpt.get();
                    
                    // Vérifier si les données ont changé
                    boolean dataChanged = false;
                    String ancienNom = etudiant.getNom();
                    String ancienPrenom = etudiant.getPrenom();
                    
                    if (!etudiant.getNom().equals(data.nom) || !etudiant.getPrenom().equals(data.prenom)) {
                        dataChanged = true;
                        
                        // Enregistrer l'historique
                        historiqueService.enregistrerModification(etudiant, 
                            etudiant.getCne(), ancienNom, ancienPrenom,
                            data.cne, data.nom, data.prenom, utilisateur);
                        
                        // Mettre à jour les données
                        etudiant.setNom(data.nom);
                        etudiant.setPrenom(data.prenom);
                        etudiantRepository.save(etudiant);
                        
                        result.getWarnings().add("Ligne " + data.rowNumber + ": Données de l'étudiant " + data.cne + " mises à jour");
                    }
                    
                    // Vérifier si l'inscription existe déjà
                    Optional<Inscription> inscriptionExistante = inscriptionRepository
                        .findByEtudiantNiveauAndAnnee(etudiant.getIdEtudiant(), niveau.getIdNiveau(), anneeUniversitaire);
                    
                    if (!inscriptionExistante.isPresent()) {
                        // Créer la nouvelle inscription
                        Inscription inscription = new Inscription();
                        inscription.setEtudiant(etudiant);
                        inscription.setNiveau(niveau);
                        inscription.setAnneeUniversitaire(anneeUniversitaire);
                        inscription.setTypeInscription(TypeInscription.REINSCRIPTION);
                        inscription = inscriptionRepository.save(inscription);
                        
                        // Inscrire automatiquement dans tous les modules du niveau
                        inscrireAuxModulesDuNiveau(inscription, niveau);
                        System.out.println("DEBUG: Réinscription aux modules terminée pour: " + data.cne);
                        
                        result.setNbReinscriptions(result.getNbReinscriptions() + 1);
                        
                        // Enregistrer dans le journal
                        journalService.enregistrerAction(utilisateur, "REINSCRIPTION_ETUDIANT", 
                            "Réinscription de l'étudiant " + etudiant.getNomComplet() + " (CNE: " + etudiant.getCne() + ") en " + niveau.getAlias());
                    } else {
                        result.getWarnings().add("Ligne " + data.rowNumber + ": L'étudiant " + data.cne + " est déjà inscrit en " + niveau.getAlias() + " pour l'année " + anneeUniversitaire);
                    }
                }
                
            } catch (Exception e) {
                System.out.println("DEBUG: Erreur lors du traitement de l'étudiant ligne " + data.rowNumber + ": " + e.getMessage());
                e.printStackTrace();
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
