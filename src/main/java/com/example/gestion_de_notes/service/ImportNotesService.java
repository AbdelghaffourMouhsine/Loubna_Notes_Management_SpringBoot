package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.ImportNotesResultDTO;
import com.example.gestion_de_notes.dto.ValidationImportNotesDTO;
import com.example.gestion_de_notes.entity.Element;
import com.example.gestion_de_notes.entity.EnseignantModuleAnnee;
import com.example.gestion_de_notes.entity.Etudiant;
import com.example.gestion_de_notes.entity.InscriptionModule;
import com.example.gestion_de_notes.entity.Note;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.entity.Session;
import com.example.gestion_de_notes.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class ImportNotesService {

    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private ElementRepository elementRepository;
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private EnseignantModuleAnneeRepository enseignantModuleAnneeRepository;
    
    @Autowired
    private JournalApplicationService journalApplicationService;
    
    @Autowired
    private InscriptionModuleRepository inscriptionModuleRepository;

    /**
     * Importe les notes depuis un fichier Excel de module
     */
    public ImportNotesResultDTO importerNotesModule(MultipartFile fichier) throws IOException {
        ImportNotesResultDTO resultat = new ImportNotesResultDTO();
        
        try {
            // 1. Validation du format du fichier
            ValidationImportNotesDTO validation = validerFormatFichier(fichier);
            if (!validation.isValide()) {
                resultat.setSucces(false);
                resultat.setMessage(validation.getMessageErreur());
                return resultat;
            }
            
            // 2. Lecture et analyse du fichier Excel
            Workbook workbook = new XSSFWorkbook(fichier.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            // 3. Validation de la structure et des données initiales
            validation = validerStructureFichier(sheet);
            if (!validation.isValide()) {
                resultat.setSucces(false);
                resultat.setMessage(validation.getMessageErreur());
                workbook.close();
                return resultat;
            }
            
            // 4. Extraction des métadonnées depuis l'en-tête
            Map<String, String> metadonnees = extraireMetadonneesFichier(sheet);
            
            // 5. Validation des données métier
            validation = validerDonneesMetier(metadonnees);
            if (!validation.isValide()) {
                resultat.setSucces(false);
                resultat.setMessage(validation.getMessageErreur());
                workbook.close();
                return resultat;
            }
            
            // 6. Validation des notes saisies
            validation = validerNotesSaisies(sheet);
            if (!validation.isValide()) {
                resultat.setSucces(false);
                resultat.setMessage(validation.getMessageErreur());
                workbook.close();
                return resultat;
            }
            
            // 7. Vérification des notes existantes et gestion des doublons
            com.example.gestion_de_notes.entity.Module module = rechercherModule(metadonnees);
            boolean notesExistantes = verifierNotesExistantes(module, metadonnees.get("session"), 
                                                            metadonnees.get("anneeUniversitaire"));
            
            if (notesExistantes) {
                resultat.setSucces(false);
                resultat.setMessage("Des notes existent déjà pour ce module et cette session. " +
                                  "Veuillez confirmer la mise à jour des données existantes.");
                resultat.setNecessiteConfirmation(true);
                resultat.setMetadonnees(metadonnees);
                workbook.close();
                return resultat;
            }
            
            // 8. Import des notes
            int nbNotesImportees = procederImportNotes(sheet, metadonnees, module);
            
            resultat.setSucces(true);
            resultat.setMessage("Import réussi : " + nbNotesImportees + " notes importées");
            resultat.setNbNotesImportees(nbNotesImportees);
            
            // 9. Journalisation
            journalApplicationService.enregistrerAction(
                "Import notes module",
                String.format("Import réussi pour %s - Session: %s - %d notes importées",
                    module.getCode(), metadonnees.get("session"), nbNotesImportees)
            );
            
            workbook.close();
            
        } catch (Exception e) {
            resultat.setSucces(false);
            resultat.setMessage("Erreur lors de l'import : " + e.getMessage());
        }
        
        return resultat;
    }

    /**
     * Importe les notes avec confirmation de mise à jour
     */
    public ImportNotesResultDTO importerNotesAvecConfirmation(MultipartFile fichier, 
                                                             Map<String, String> metadonnees) throws IOException {
        ImportNotesResultDTO resultat = new ImportNotesResultDTO();
        
        try {
            Workbook workbook = new XSSFWorkbook(fichier.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            com.example.gestion_de_notes.entity.Module module = rechercherModule(metadonnees);
            
            // Supprimer les notes existantes
            supprimerNotesExistantes(module, metadonnees.get("session"), 
                                   metadonnees.get("anneeUniversitaire"));
            
            // Procéder à l'import
            int nbNotesImportees = procederImportNotes(sheet, metadonnees, module);
            
            resultat.setSucces(true);
            resultat.setMessage("Mise à jour réussie : " + nbNotesImportees + " notes importées");
            resultat.setNbNotesImportees(nbNotesImportees);
            
            // Journalisation
            journalApplicationService.enregistrerAction(
                "Mise à jour notes module",
                String.format("Mise à jour réussie pour %s - Session: %s - %d notes importées",
                    module.getCode(), metadonnees.get("session"), nbNotesImportees)
            );
            
            workbook.close();
            
        } catch (Exception e) {
            resultat.setSucces(false);
            resultat.setMessage("Erreur lors de la mise à jour : " + e.getMessage());
        }
        
        return resultat;
    }

    /**
     * Valide le format du fichier (Excel .xlsx)
     */
    private ValidationImportNotesDTO validerFormatFichier(MultipartFile fichier) {
        ValidationImportNotesDTO validation = new ValidationImportNotesDTO();
        
        if (fichier == null || fichier.isEmpty()) {
            validation.setValide(false);
            validation.setMessageErreur("Aucun fichier sélectionné");
            return validation;
        }
        
        String nomFichier = fichier.getOriginalFilename();
        if (nomFichier == null || !nomFichier.toLowerCase().endsWith(".xlsx")) {
            validation.setValide(false);
            validation.setMessageErreur("Le fichier doit être au format Excel (.xlsx)");
            return validation;
        }
        
        // Vérification de la taille (max 10MB)
        if (fichier.getSize() > 10 * 1024 * 1024) {
            validation.setValide(false);
            validation.setMessageErreur("Le fichier est trop volumineux (maximum 10MB)");
            return validation;
        }
        
        validation.setValide(true);
        return validation;
    }

    /**
     * Valide la structure du fichier Excel
     */
    private ValidationImportNotesDTO validerStructureFichier(Sheet sheet) {
        ValidationImportNotesDTO validation = new ValidationImportNotesDTO();
        
        try {
            // Vérifier qu'il y a au moins 4 lignes (en-tête + colonnes + au moins 1 étudiant)
            if (sheet.getLastRowNum() < 3) {
                validation.setValide(false);
                validation.setMessageErreur("Structure de fichier incorrecte : nombre de lignes insuffisant");
                return validation;
            }
            
            // Vérifier la présence des lignes d'en-tête
            Row premiereLigne = sheet.getRow(0);
            Row deuxiemeLigne = sheet.getRow(1);
            
            if (premiereLigne == null || deuxiemeLigne == null) {
                validation.setValide(false);
                validation.setMessageErreur("Structure de fichier incorrecte : en-tête manquant");
                return validation;
            }
            
            // Vérifier les étiquettes de la première ligne
            if (!estCelluleEgale(premiereLigne.getCell(0), "Module") ||
                !estCelluleEgale(premiereLigne.getCell(2), "Semestre") ||
                !estCelluleEgale(premiereLigne.getCell(4), "Année")) {
                validation.setValide(false);
                validation.setMessageErreur("Structure de fichier incorrecte : étiquettes de la première ligne modifiées");
                return validation;
            }
            
            // Vérifier les étiquettes de la deuxième ligne
            if (!estCelluleEgale(deuxiemeLigne.getCell(0), "Enseignant") ||
                !estCelluleEgale(deuxiemeLigne.getCell(2), "Session") ||
                !estCelluleEgale(deuxiemeLigne.getCell(4), "Classe")) {
                validation.setValide(false);
                validation.setMessageErreur("Structure de fichier incorrecte : étiquettes de la deuxième ligne modifiées");
                return validation;
            }
            
            // Vérifier les en-têtes des colonnes de données (ligne 3, index 2)
            Row enteteColonnes = sheet.getRow(3);
            if (enteteColonnes == null) {
                validation.setValide(false);
                validation.setMessageErreur("Structure de fichier incorrecte : ligne d'en-tête des colonnes manquante");
                return validation;
            }
            
            if (!estCelluleEgale(enteteColonnes.getCell(0), "ID") ||
                !estCelluleEgale(enteteColonnes.getCell(1), "CNE") ||
                !estCelluleEgale(enteteColonnes.getCell(2), "NOM") ||
                !estCelluleEgale(enteteColonnes.getCell(3), "PRENOM")) {
                validation.setValide(false);
                validation.setMessageErreur("Structure de fichier incorrecte : colonnes de base modifiées");
                return validation;
            }
            
            validation.setValide(true);
            return validation;
            
        } catch (Exception e) {
            validation.setValide(false);
            validation.setMessageErreur("Erreur lors de la validation de la structure : " + e.getMessage());
            return validation;
        }
    }

    /**
     * Vérifie si une cellule contient la valeur attendue
     */
    private boolean estCelluleEgale(Cell cellule, String valeurAttendue) {
        if (cellule == null) return false;
        
        String valeurCellule = "";
        if (cellule.getCellType() == CellType.STRING) {
            valeurCellule = cellule.getStringCellValue();
        } else if (cellule.getCellType() == CellType.NUMERIC) {
            valeurCellule = String.valueOf(cellule.getNumericCellValue());
        }
        
        return valeurAttendue.equals(valeurCellule.trim());
    }

    /**
     * Extrait les métadonnées depuis l'en-tête du fichier
     */
    private Map<String, String> extraireMetadonneesFichier(Sheet sheet) {
        Map<String, String> metadonnees = new HashMap<>();
        
        Row premiereLigne = sheet.getRow(0);
        Row deuxiemeLigne = sheet.getRow(1);
        
        // Extraction des données de la première ligne
        metadonnees.put("moduleCode", obtenirValeurCellule(premiereLigne.getCell(1)));
        metadonnees.put("semestre", obtenirValeurCellule(premiereLigne.getCell(3)));
        metadonnees.put("anneeUniversitaire", obtenirValeurCellule(premiereLigne.getCell(5)));
        
        // Extraction des données de la deuxième ligne
        metadonnees.put("enseignantNom", obtenirValeurCellule(deuxiemeLigne.getCell(1)));
        metadonnees.put("session", obtenirValeurCellule(deuxiemeLigne.getCell(3)));
        metadonnees.put("classeAlias", obtenirValeurCellule(deuxiemeLigne.getCell(5)));
        
        return metadonnees;
    }

    /**
     * Obtient la valeur d'une cellule sous forme de chaîne
     */
    private String obtenirValeurCellule(Cell cellule) {
        if (cellule == null) return "";
        
        switch (cellule.getCellType()) {
            case STRING:
                return cellule.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cellule)) {
                    return cellule.getDateCellValue().toString();
                } else {
                    return String.valueOf(cellule.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cellule.getBooleanCellValue());
            default:
                return "";
        }
    }


    /**
     * Valide les données métier (module, enseignant, etc.)
     */
    private ValidationImportNotesDTO validerDonneesMetier(Map<String, String> metadonnees) {
        ValidationImportNotesDTO validation = new ValidationImportNotesDTO();
        
        try {
            // 1. Validation du module
            com.example.gestion_de_notes.entity.Module module = rechercherModule(metadonnees);
            if (module == null) {
                validation.setValide(false);
                validation.setMessageErreur("Module non trouvé : " + metadonnees.get("moduleCode"));
                return validation;
            }
            
            // 2. Validation de la cohérence module-classe
            if (!module.getNiveau().getAlias().equals(metadonnees.get("classeAlias"))) {
                validation.setValide(false);
                validation.setMessageErreur("Incohérence : le module " + metadonnees.get("moduleCode") + 
                                          " n'appartient pas à la classe " + metadonnees.get("classeAlias"));
                return validation;
            }
            
            // 3. Validation de l'enseignant
            if (!validerEnseignantModule(module, metadonnees.get("enseignantNom"), 
                                       metadonnees.get("anneeUniversitaire"))) {
                validation.setValide(false);
                validation.setMessageErreur("Enseignant incorrect pour ce module : " + metadonnees.get("enseignantNom"));
                return validation;
            }
            
            // 4. Validation de l'année universitaire (format)
            if (!validerFormatAnneeUniversitaire(metadonnees.get("anneeUniversitaire"))) {
                validation.setValide(false);
                validation.setMessageErreur("Format d'année universitaire incorrect : " + metadonnees.get("anneeUniversitaire"));
                return validation;
            }
            
            // 5. Validation de la session
            String session = metadonnees.get("session");
            if (!"NORMALE".equals(session) && !"RATTRAPAGE".equals(session)) {
                validation.setValide(false);
                validation.setMessageErreur("Session incorrecte : " + session + " (doit être NORMALE ou RATTRAPAGE)");
                return validation;
            }
            
            validation.setValide(true);
            return validation;
            
        } catch (Exception e) {
            validation.setValide(false);
            validation.setMessageErreur("Erreur lors de la validation des données métier : " + e.getMessage());
            return validation;
        }
    }

    /**
     * Recherche un module par son code
     */
    private com.example.gestion_de_notes.entity.Module rechercherModule(Map<String, String> metadonnees) {
        Optional<com.example.gestion_de_notes.entity.Module> moduleOpt = moduleRepository.findByCode(metadonnees.get("moduleCode"));
        return moduleOpt.orElse(null);
    }

    /**
     * Valide l'enseignant assigné au module
     */
    private boolean validerEnseignantModule(com.example.gestion_de_notes.entity.Module module, String enseignantNom, String anneeUniversitaire) {
        List<EnseignantModuleAnnee> enseignants = enseignantModuleAnneeRepository
                .findByModuleAndAnneeUniversitaire(module, anneeUniversitaire);
        
        if (enseignants.isEmpty()) {
            return false; // Aucun enseignant assigné
        }
        
        // Vérifier si le nom correspond
        for (EnseignantModuleAnnee enseignantModule : enseignants) {
            Personne enseignant = enseignantModule.getEnseignant();
            String nomComplet = enseignant.getNom() + " " + enseignant.getPrenom();
            if (nomComplet.trim().equalsIgnoreCase(enseignantNom.trim())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Valide le format de l'année universitaire (ex: 2024/2025)
     */
    private boolean validerFormatAnneeUniversitaire(String annee) {
        if (annee == null || annee.trim().isEmpty()) {
            return false;
        }
        
        return annee.matches("\\d{4}/\\d{4}");
    }

    /**
     * Valide les notes saisies dans le fichier
     */
    private ValidationImportNotesDTO validerNotesSaisies(Sheet sheet) {
        ValidationImportNotesDTO validation = new ValidationImportNotesDTO();
        
        try {
            Row enteteColonnes = sheet.getRow(3);
            
            // Trouver les colonnes d'éléments (entre PRENOM et Moyenne)
            int colonneMoyenne = trouverColonneMoyenne(enteteColonnes);
            if (colonneMoyenne == -1) {
                validation.setValide(false);
                validation.setMessageErreur("Colonne 'Moyenne' non trouvée");
                return validation;
            }
            
            int colonneValidation = colonneMoyenne + 1;
            
            // Valider les formules dans les colonnes Moyenne et Validation
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Vérifier que les notes sont dans la plage 0-20
                for (int j = 4; j < colonneMoyenne; j++) {
                    Cell cellule = row.getCell(j);
                    if (cellule != null && cellule.getCellType() == CellType.NUMERIC) {
                        double note = cellule.getNumericCellValue();
                        if (note < 0.0 || note > 20.0) {
                            validation.setValide(false);
                            validation.setMessageErreur("Note invalide à la ligne " + (i + 1) + 
                                                      ", colonne " + (j + 1) + " : " + note + 
                                                      " (doit être entre 0.0 et 20.0)");
                            return validation;
                        }
                    }
                }
                
                // Vérifier les formules (Moyenne et Validation doivent être des formules)
                Cell celluleMoyenne = row.getCell(colonneMoyenne);
                Cell celluleValidation = row.getCell(colonneValidation);
                
                if (celluleMoyenne != null && celluleMoyenne.getCellType() != CellType.FORMULA) {
                    validation.setValide(false);
                    validation.setMessageErreur("La colonne Moyenne doit contenir des formules (ligne " + (i + 1) + ")");
                    return validation;
                }
                
                if (celluleValidation != null && celluleValidation.getCellType() != CellType.FORMULA) {
                    validation.setValide(false);
                    validation.setMessageErreur("La colonne Validation doit contenir des formules (ligne " + (i + 1) + ")");
                    return validation;
                }
            }
            
            validation.setValide(true);
            return validation;
            
        } catch (Exception e) {
            validation.setValide(false);
            validation.setMessageErreur("Erreur lors de la validation des notes : " + e.getMessage());
            return validation;
        }
    }

    /**
     * Trouve la colonne "Moyenne" dans l'en-tête
     */
    private int trouverColonneMoyenne(Row entete) {
        for (int i = 0; i < entete.getLastCellNum(); i++) {
            Cell cellule = entete.getCell(i);
            if (cellule != null && "Moyenne".equals(cellule.getStringCellValue())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Vérifie si des notes existent déjà pour ce module et cette session
     */
    private boolean verifierNotesExistantes(com.example.gestion_de_notes.entity.Module module, String session, String anneeUniversitaire) {
        Session sessionEnum = Session.valueOf(session);
        List<Note> notesExistantes = noteRepository.findByModuleSessionAndAnnee(
            module.getIdModule(), sessionEnum, anneeUniversitaire
        );
        return !notesExistantes.isEmpty();
    }

    /**
     * Supprime les notes existantes pour un module et une session
     */
    private void supprimerNotesExistantes(com.example.gestion_de_notes.entity.Module module, String session, String anneeUniversitaire) {
        Session sessionEnum = Session.valueOf(session);
        List<Note> notesExistantes = noteRepository.findByModuleSessionAndAnnee(
            module.getIdModule(), sessionEnum, anneeUniversitaire
        );
        noteRepository.deleteAll(notesExistantes);
    }

    /**
     * Procède à l'import effectif des notes
     */
    private int procederImportNotes(Sheet sheet, Map<String, String> metadonnees, com.example.gestion_de_notes.entity.Module module) {
        int nbNotesImportees = 0;
        
        try {
            Row enteteColonnes = sheet.getRow(3);
            List<Element> elements = elementRepository.findByModuleIdModule(module.getIdModule());
            
            // Créer une map des éléments par titre pour la correspondance
            Map<String, Element> elementsParTitre = new HashMap<>();
            for (Element element : elements) {
                elementsParTitre.put(element.getTitre(), element);
            }
            
            // Identifier les colonnes des éléments
            List<Integer> colonnesElements = new ArrayList<>();
            List<Element> elementsOrdonnees = new ArrayList<>();
            
            for (int i = 4; i < enteteColonnes.getLastCellNum(); i++) {
                Cell cellule = enteteColonnes.getCell(i);
                if (cellule != null) {
                    String titre = cellule.getStringCellValue();
                    if (elementsParTitre.containsKey(titre)) {
                        colonnesElements.add(i);
                        elementsOrdonnees.add(elementsParTitre.get(titre));
                    } else if ("Moyenne".equals(titre)) {
                        break; // Arrêter à la colonne Moyenne
                    }
                }
            }
            
            Session sessionEnum = Session.valueOf(metadonnees.get("session"));
            String anneeUniversitaire = metadonnees.get("anneeUniversitaire");
            
            // Traiter chaque ligne d'étudiant
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Récupérer l'ID de l'étudiant
                Cell celluleId = row.getCell(0);
                if (celluleId == null || celluleId.getCellType() != CellType.NUMERIC) {
                    continue; // Passer cette ligne si pas d'ID valide
                }
                
                Long etudiantId = (long) celluleId.getNumericCellValue();
                
                // Vérifier que l'étudiant existe
                Optional<Etudiant> etudiantOpt = etudiantRepository.findById(etudiantId);
                if (etudiantOpt.isEmpty()) {
                    continue; // Passer cette ligne si étudiant non trouvé
                }
                
                Etudiant etudiant = etudiantOpt.get();
                
                // Vérifier que l'étudiant est inscrit dans ce module
                if (!verifierInscriptionModuleEtudiant(etudiantId, module.getIdModule())) {
                    continue; // Passer cette ligne si pas inscrit
                }
                
                // Importer les notes pour chaque élément
                for (int j = 0; j < colonnesElements.size(); j++) {
                    Cell celluleNote = row.getCell(colonnesElements.get(j));
                    if (celluleNote != null && celluleNote.getCellType() == CellType.NUMERIC) {
                        double valeurNote = celluleNote.getNumericCellValue();
                        
                        // Créer et sauvegarder la note
                        Note note = new Note();
                        note.setEtudiant(etudiant);
                        note.setElement(elementsOrdonnees.get(j));
                        note.setValeurNote(BigDecimal.valueOf(valeurNote));
                        note.setSession(sessionEnum);
                        note.setAnneeUniversitaire(anneeUniversitaire);
                        
                        noteRepository.save(note);
                        nbNotesImportees++;
                    }
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'import des notes : " + e.getMessage(), e);
        }
        
        return nbNotesImportees;
    }

    /**
     * Vérifie qu'un étudiant est inscrit dans un module
     */
    private boolean verifierInscriptionModuleEtudiant(Long etudiantId, Long moduleId) {
        return inscriptionModuleRepository.findByEtudiantAndModule(etudiantId, moduleId).isPresent();
    }
}
