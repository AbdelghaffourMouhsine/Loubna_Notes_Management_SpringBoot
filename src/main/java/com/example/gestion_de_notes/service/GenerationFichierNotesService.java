package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.*;
import com.example.gestion_de_notes.entity.Element;
import com.example.gestion_de_notes.entity.EnseignantModuleAnnee;
import com.example.gestion_de_notes.entity.Etudiant;
import com.example.gestion_de_notes.entity.Inscription;
import com.example.gestion_de_notes.entity.InscriptionModule;
import com.example.gestion_de_notes.entity.Module;
import com.example.gestion_de_notes.entity.Niveau;
import com.example.gestion_de_notes.entity.Note;
import com.example.gestion_de_notes.entity.ParametreValidation;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.entity.Session;
import com.example.gestion_de_notes.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GenerationFichierNotesService {
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private ElementRepository elementRepository;
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private InscriptionModuleRepository inscriptionModuleRepository;
    
    @Autowired
    private ParametreValidationService parametreValidationService;
    
    @Autowired
    private EnseignantModuleAnneeRepository enseignantModuleAnneeRepository;
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private JournalApplicationService journalApplicationService;
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    @Autowired
    private InscriptionRepository inscriptionRepository;
    
    /**
     * Génère un fichier Excel de collecte des notes pour un module
     */
    public byte[] genererFichierNotesModule(GenerationFichierRequestDTO request) throws IOException {
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));
        
        List<Element> elements = elementRepository.findByModuleIdModule(request.getModuleId());
        List<EtudiantNotesDTO> etudiants = getEtudiantsInscritsModule(module, request);
        
        // Récupération des paramètres de validation
        Optional<ParametreValidationDTO> parametres = parametreValidationService
                .findByFiliereAndNiveau(module.getNiveau().getFilieres().get(0).getIdFiliere(), 
                                      module.getNiveau().getIdNiveau());
        
        BigDecimal seuilNormal = parametres.map(ParametreValidationDTO::getSeuilValidationNormale)
                .orElse(new BigDecimal("12.00"));
        BigDecimal seuilRattrapage = parametres.map(ParametreValidationDTO::getSeuilValidationRattrapage)
                .orElse(new BigDecimal("8.00"));
        
        byte[] fichierExcel = creerFichierExcelModule(module, elements, etudiants, request, 
                                                     seuilNormal, seuilRattrapage);
        
        // Journalisation
        journalApplicationService.enregistrerAction(
            "Génération fichier notes module",
            String.format("Fichier généré pour %s - Session: %s - %d étudiants",
                module.getCode(), request.getSession(), etudiants.size())
        );
        
        return fichierExcel;
    }
    
    /**
     * Génère un fichier Excel de délibération finale pour un niveau
     */
    public byte[] genererFichierDeliberationNiveau(GenerationFichierRequestDTO request) throws IOException {
        Niveau niveau = niveauRepository.findById(request.getNiveauId())
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));
        
        // Récupération de tous les modules du niveau
        List<Module> modules = moduleRepository.findByNiveau(niveau);
        
        // Récupération des étudiants du niveau avec leurs notes
        List<EtudiantDeliberationDTO> etudiants = getEtudiantsAvecNotesDeliberation(niveau, modules, request);
        
        // Calcul des moyennes générales et des rangs
        calculerMoyennesEtRangs(etudiants);
        
        // Création du fichier Excel
        byte[] fichierExcel = creerFichierExcelDeliberation(niveau, modules, etudiants, request);
        
        // Journalisation
        journalApplicationService.enregistrerAction(
            "Génération fichier délibération",
            String.format("Fichier délibération généré pour %s - %d étudiants, %d modules",
                niveau.getAlias(), etudiants.size(), modules.size())
        );
        
        return fichierExcel;
    }
    
    /**
     * Récupère les étudiants inscrits dans un module pour une session donnée
     */
    private List<EtudiantNotesDTO> getEtudiantsInscritsModule(Module module, GenerationFichierRequestDTO request) {
        List<InscriptionModule> inscriptions = inscriptionModuleRepository
                .findByModuleAndAnneeUniversitaire(module, request.getAnneeUniversitaire());
        
        List<EtudiantNotesDTO> etudiants = inscriptions.stream()
                .map(inscription -> new EtudiantNotesDTO(
                    inscription.getInscription().getEtudiant().getIdEtudiant(),
                    inscription.getInscription().getEtudiant().getCne(),
                    inscription.getInscription().getEtudiant().getNom(),
                    inscription.getInscription().getEtudiant().getPrenom()
                ))
                .collect(Collectors.toList());
        
        // Filtrage pour session de rattrapage si nécessaire
        if ("RATTRAPAGE".equals(request.getSession())) {
            etudiants = filtrerEtudiantsRattrapage(etudiants, module, request.getAnneeUniversitaire());
        }
        
        return etudiants;
    }
    
    /**
     * Filtre les étudiants qui ont besoin de rattrapage
     */
    private List<EtudiantNotesDTO> filtrerEtudiantsRattrapage(List<EtudiantNotesDTO> etudiants, 
                                                            Module module, String anneeUniversitaire) {
        return etudiants.stream()
                .filter(etudiant -> {
                    // Vérifier si l'étudiant a des notes en session normale nécessitant un rattrapage
                    List<Note> notesNormales = noteRepository
                            .findByEtudiantAndModuleAndSession(etudiant.getEtudiantId(), 
                                                             module.getIdModule(), "NORMALE");
                    
                    if (notesNormales.isEmpty()) {
                        return false; // Pas de notes en session normale
                    }
                    
                    // Calculer la moyenne et vérifier si rattrapage nécessaire
                    BigDecimal moyenne = calculerMoyenneModule(notesNormales);
                    return moyenne != null && moyenne.compareTo(new BigDecimal("12.00")) < 0 
                           && moyenne.compareTo(new BigDecimal("8.00")) >= 0;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Calcule la moyenne d'un module à partir des notes
     */
    private BigDecimal calculerMoyenneModule(List<Note> notes) {
        if (notes.isEmpty()) {
            return null;
        }
        
        BigDecimal somme = notes.stream()
                .map(Note::getValeurNote)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return somme.divide(new BigDecimal(notes.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Crée le fichier Excel pour un module
     */
    private byte[] creerFichierExcelModule(Module module, List<Element> elements, 
                                         List<EtudiantNotesDTO> etudiants, 
                                         GenerationFichierRequestDTO request,
                                         BigDecimal seuilNormal, BigDecimal seuilRattrapage) throws IOException {
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Notes " + module.getCode());
        
        // Styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle formulaStyle = createFormulaStyle(workbook);
        
        int rowNum = 0;
        
        // En-tête du fichier
        rowNum = creerEnteteModule(sheet, module, request, headerStyle, rowNum);
        
        // En-têtes des colonnes
        rowNum = creerEnteteColonnes(sheet, elements, headerStyle, rowNum);
        
        // Données des étudiants
        rowNum = ajouterDonneesEtudiants(sheet, etudiants, elements, dataStyle, formulaStyle, 
                                       rowNum, seuilNormal, seuilRattrapage);
        
        // Ajustement automatique des colonnes
        ajusterLargeurColonnes(sheet, elements.size());
        
        // Conversion en byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * Crée l'en-tête du fichier avec les informations du module
     */
    private int creerEnteteModule(Sheet sheet, Module module, GenerationFichierRequestDTO request,
                                CellStyle headerStyle, int startRow) {
        int rowNum = startRow;
        
        // Première ligne : Module | m1 | Semestre | Automne | Année | 2024/2025
        Row firstRow = sheet.createRow(rowNum++);
        firstRow.createCell(0).setCellValue("Module");
        firstRow.createCell(1).setCellValue(module.getCode());
        firstRow.createCell(2).setCellValue("Semestre");
        firstRow.createCell(3).setCellValue(request.getSemestre());
        firstRow.createCell(4).setCellValue("Année");
        firstRow.createCell(5).setCellValue(request.getAnneeUniversitaire());
        
        // Deuxième ligne : Enseignant | mouhsine abdelghaffour | Session | NORMALE | Classe | CP1
        String enseignantNom = getEnseignantModule(module, request.getAnneeUniversitaire());
        Row secondRow = sheet.createRow(rowNum++);
        secondRow.createCell(0).setCellValue("Enseignant");
        secondRow.createCell(1).setCellValue(enseignantNom);
        secondRow.createCell(2).setCellValue("Session");
        secondRow.createCell(3).setCellValue(request.getSession());
        secondRow.createCell(4).setCellValue("Classe");
        secondRow.createCell(5).setCellValue(module.getNiveau().getAlias());
        
        // Application du style aux cellules des étiquettes (colonnes 0, 2, 4)
        firstRow.getCell(0).setCellStyle(headerStyle);
        firstRow.getCell(2).setCellStyle(headerStyle);
        firstRow.getCell(4).setCellStyle(headerStyle);
        secondRow.getCell(0).setCellStyle(headerStyle);
        secondRow.getCell(2).setCellStyle(headerStyle);
        secondRow.getCell(4).setCellStyle(headerStyle);
        
        // Ligne vide
        sheet.createRow(rowNum++);
        
        return rowNum;
    }
    
    /**
     * Récupère le nom de l'enseignant du module
     */
    private String getEnseignantModule(Module module, String anneeUniversitaire) {
        List<EnseignantModuleAnnee> enseignants = enseignantModuleAnneeRepository
                .findByModuleAndAnneeUniversitaire(module, anneeUniversitaire);
        
        if (!enseignants.isEmpty()) {
            Personne enseignant = enseignants.get(0).getEnseignant();
            return enseignant.getNom() + " " + enseignant.getPrenom();
        }
        
        return "Non défini";
    }
    
    /**
     * Crée les en-têtes des colonnes
     */
    private int creerEnteteColonnes(Sheet sheet, List<Element> elements, CellStyle headerStyle, int startRow) {
        Row headerRow = sheet.createRow(startRow);
        
        int colNum = 0;
        headerRow.createCell(colNum++).setCellValue("ID");
        headerRow.createCell(colNum++).setCellValue("CNE");
        headerRow.createCell(colNum++).setCellValue("NOM");
        headerRow.createCell(colNum++).setCellValue("PRENOM");
        
        // Colonnes pour les éléments
        for (Element element : elements) {
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(element.getTitre());
            cell.setCellStyle(headerStyle);
        }
        
        headerRow.createCell(colNum++).setCellValue("Moyenne");
        headerRow.createCell(colNum).setCellValue("Validation");
        
        // Appliquer le style aux en-têtes
        for (int i = 0; i <= colNum; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        return startRow + 1;
    }
    
    /**
     * Ajoute les données des étudiants avec formules
     */
    private int ajouterDonneesEtudiants(Sheet sheet, List<EtudiantNotesDTO> etudiants, 
                                      List<Element> elements, CellStyle dataStyle, 
                                      CellStyle formulaStyle, int startRow,
                                      BigDecimal seuilNormal, BigDecimal seuilRattrapage) {
        int rowNum = startRow;
        
        for (EtudiantNotesDTO etudiant : etudiants) {
            Row row = sheet.createRow(rowNum);
            int colNum = 0;
            
            // Données de base de l'étudiant
            row.createCell(colNum++).setCellValue(etudiant.getEtudiantId());
            row.createCell(colNum++).setCellValue(etudiant.getCne());
            row.createCell(colNum++).setCellValue(etudiant.getNom());
            row.createCell(colNum++).setCellValue(etudiant.getPrenom());
            
            // Colonnes pour les notes des éléments (vides, à remplir par l'enseignant)
            int firstElementCol = colNum;
            for (int i = 0; i < elements.size(); i++) {
                Cell cell = row.createCell(colNum++);
                cell.setCellStyle(dataStyle);
                // Les notes seront saisies manuellement
            }
            
            // Formule pour la moyenne
            Cell moyenneCell = row.createCell(colNum++);
            String moyenneFormule = creerFormuleMoyenne(firstElementCol, elements.size(), rowNum + 1);
            moyenneCell.setCellFormula(moyenneFormule);
            moyenneCell.setCellStyle(formulaStyle);
            
            // Formule pour la validation
            Cell validationCell = row.createCell(colNum);
            String validationFormule = creerFormuleValidation(colNum, rowNum + 1, seuilNormal, seuilRattrapage);
            validationCell.setCellFormula(validationFormule);
            validationCell.setCellStyle(formulaStyle);
            
            rowNum++;
        }
        
        return rowNum;
    }
    
    /**
     * Crée la formule de calcul de la moyenne
     */
    private String creerFormuleMoyenne(int firstElementCol, int nbElements, int rowNumber) {
        if (nbElements == 0) {
            return "0";
        }
        
        char startCol = (char) ('A' + firstElementCol);
        char endCol = (char) ('A' + firstElementCol + nbElements - 1);
        
        return String.format("AVERAGE(%c%d:%c%d)", startCol, rowNumber, endCol, rowNumber);
    }
    
    /**
     * Crée la formule de validation (V/R/NV)
     */
    private String creerFormuleValidation(int moyenneCol, int rowNumber, 
                                        BigDecimal seuilNormal, BigDecimal seuilRattrapage) {
        char moyenneColChar = (char) ('A' + moyenneCol - 1); // -1 car moyenneCol est la colonne suivante
        
        return String.format("IF(%c%d>=%s,\"V\",IF(%c%d>=%s,\"R\",\"NV\"))",
                moyenneColChar, rowNumber, seuilNormal.toString(),
                moyenneColChar, rowNumber, seuilRattrapage.toString());
    }
    
    /**
     * Crée le style pour les en-têtes
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    /**
     * Crée le style pour les données
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    /**
     * Crée le style pour les formules
     */
    private CellStyle createFormulaStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * Ajuste automatiquement la largeur des colonnes
     */
    private void ajusterLargeurColonnes(Sheet sheet, int nbElements) {
        // Colonnes de base (ID, CNE, NOM, PRENOM)
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Colonnes des éléments
        for (int i = 4; i < 4 + nbElements; i++) {
            sheet.setColumnWidth(i, 3000); // Largeur fixe pour les notes
        }
        
        // Colonne moyenne et validation
        sheet.setColumnWidth(4 + nbElements, 3000);
        sheet.setColumnWidth(4 + nbElements + 1, 3000);
    }
    
    /**
     * Récupère les étudiants d'un niveau avec toutes leurs notes pour la délibération
     */
    private List<EtudiantDeliberationDTO> getEtudiantsAvecNotesDeliberation(Niveau niveau, 
                                                                           List<Module> modules, 
                                                                           GenerationFichierRequestDTO request) {
        
        // Récupération de tous les étudiants inscrits dans le niveau
        List<Inscription> inscriptions = inscriptionRepository
                .findByNiveauAndAnneeUniversitaire(niveau, request.getAnneeUniversitaire());
        
        List<EtudiantDeliberationDTO> etudiants = new ArrayList<>();
        
        for (Inscription inscription : inscriptions) {
            EtudiantDeliberationDTO etudiantDTO = new EtudiantDeliberationDTO(
                inscription.getEtudiant().getIdEtudiant(),
                inscription.getEtudiant().getCne(),
                inscription.getEtudiant().getNom(),
                inscription.getEtudiant().getPrenom()
            );
            
            etudiantDTO.setNiveauAlias(niveau.getAlias());
            etudiantDTO.setAnneeUniversitaire(request.getAnneeUniversitaire());
            
            // Récupération des notes pour tous les modules
            Map<Long, EtudiantDeliberationDTO.ModuleNotesDTO> moduleNotes = new HashMap<>();
            
            for (Module module : modules) {
                EtudiantDeliberationDTO.ModuleNotesDTO moduleNotesDTO = 
                    getNotesModulePourEtudiant(etudiantDTO.getEtudiantId(), module, request.getAnneeUniversitaire());
                
                if (moduleNotesDTO != null) {
                    moduleNotes.put(module.getIdModule(), moduleNotesDTO);
                }
            }
            
            etudiantDTO.setModuleNotes(moduleNotes);
            
            // Gestion des modules supplémentaires pour étudiants ajournés
            List<EtudiantDeliberationDTO.ModuleNotesDTO> modulesSupplementaires = 
                getModulesSupplementairesEtudiant(etudiantDTO.getEtudiantId(), niveau, request.getAnneeUniversitaire());
            etudiantDTO.setModulesSupplementaires(modulesSupplementaires);
            
            etudiants.add(etudiantDTO);
        }
        
        return etudiants;
    }
    
    /**
     * Récupère les notes d'un module pour un étudiant
     */
    private EtudiantDeliberationDTO.ModuleNotesDTO getNotesModulePourEtudiant(Long etudiantId, Module module, String anneeUniversitaire) {
        List<Element> elements = elementRepository.findByModuleIdModule(module.getIdModule());
        
        // Vérifier si l'étudiant est inscrit dans ce module
        Optional<InscriptionModule> inscriptionModule = inscriptionModuleRepository
                .findByEtudiantAndModule(etudiantId, module.getIdModule());
        
        if (inscriptionModule.isEmpty()) {
            return null; // Étudiant pas inscrit dans ce module
        }
        
        EtudiantDeliberationDTO.ModuleNotesDTO moduleNotesDTO = new EtudiantDeliberationDTO.ModuleNotesDTO();
        moduleNotesDTO.setModuleId(module.getIdModule());
        moduleNotesDTO.setModuleCode(module.getCode());
        moduleNotesDTO.setModuleTitle(module.getTitre());
        
        // Récupération du nom de l'enseignant
        List<EnseignantModuleAnnee> enseignants = enseignantModuleAnneeRepository
                .findByModuleAndAnneeUniversitaire(module, anneeUniversitaire);
        if (!enseignants.isEmpty()) {
            Personne enseignant = enseignants.get(0).getEnseignant();
            moduleNotesDTO.setEnseignantNom(enseignant.getNom() + " " + enseignant.getPrenom());
        } else {
            moduleNotesDTO.setEnseignantNom("Non défini");
        }
        
        // Récupération des notes par élément
        Map<Long, BigDecimal> elementNotes = new HashMap<>();
        List<BigDecimal> notesValides = new ArrayList<>();
        
        for (Element element : elements) {
            // Récupération de la note finale (priorité rattrapage si existe)
            Optional<Note> noteRattrapage = noteRepository.findByEtudiantElementSessionAndAnnee(
                etudiantId, element.getIdElement(), Session.RATTRAPAGE, anneeUniversitaire
            );
            
            BigDecimal notefinale = null;
            if (noteRattrapage.isPresent()) {
                notefinale = noteRattrapage.get().getValeurNote();
            } else {
                // Pas de note de rattrapage, chercher en session normale
                Optional<Note> noteNormale = noteRepository.findByEtudiantElementSessionAndAnnee(
                    etudiantId, element.getIdElement(), Session.NORMALE, anneeUniversitaire
                );
                if (noteNormale.isPresent()) {
                    notefinale = noteNormale.get().getValeurNote();
                }
            }
            
            if (notefinale != null) {
                elementNotes.put(element.getIdElement(), notefinale);
                notesValides.add(notefinale);
            }
        }
        
        moduleNotesDTO.setElementNotes(elementNotes);
        
        // Calcul de la moyenne du module
        if (!notesValides.isEmpty()) {
            BigDecimal moyenne = notesValides.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(notesValides.size()), 2, BigDecimal.ROUND_HALF_UP);
            
            moduleNotesDTO.setMoyenneModule(moyenne);
            
            // Détermination de la validation selon les seuils
            String validation = determinerValidationModule(module, moyenne);
            moduleNotesDTO.setValidation(validation);
        }
        
        return moduleNotesDTO;
    }
    
    /**
     * Détermine la validation d'un module selon les seuils
     */
    private String determinerValidationModule(Module module, BigDecimal moyenne) {
        if (module.getNiveau().getFilieres().isEmpty()) {
            // Seuils par défaut si pas de filière
            if (moyenne.compareTo(new BigDecimal("12.00")) >= 0) return "V";
            if (moyenne.compareTo(new BigDecimal("8.00")) >= 0) return "R";
            return "NV";
        }
        
        Optional<ParametreValidationDTO> parametres = parametreValidationService
                .findByFiliereAndNiveau(module.getNiveau().getFilieres().get(0).getIdFiliere(), 
                                      module.getNiveau().getIdNiveau());
        
        if (parametres.isPresent()) {
            BigDecimal seuilNormal = parametres.get().getSeuilValidationNormale();
            BigDecimal seuilRattrapage = parametres.get().getSeuilValidationRattrapage();
            
            if (moyenne.compareTo(seuilNormal) >= 0) return "V";
            if (moyenne.compareTo(seuilRattrapage) >= 0) return "R";
            return "NV";
        }
        
        // Seuils par défaut
        if (moyenne.compareTo(new BigDecimal("12.00")) >= 0) return "V";
        if (moyenne.compareTo(new BigDecimal("8.00")) >= 0) return "R";
        return "NV";
    }
    
    /**
     * Récupère les modules supplémentaires pour un étudiant ajourné
     */
    private List<EtudiantDeliberationDTO.ModuleNotesDTO> getModulesSupplementairesEtudiant(Long etudiantId, 
                                                                                          Niveau niveau, 
                                                                                          String anneeUniversitaire) {
        List<EtudiantDeliberationDTO.ModuleNotesDTO> modulesSupp = new ArrayList<>();
        
        if (niveau.getNiveauSuivant() != null) {
            // Récupérer les modules du niveau suivant où l'étudiant est inscrit
            List<Module> modulesNiveauSuivant = moduleRepository.findByNiveau(niveau.getNiveauSuivant());
            
            for (Module module : modulesNiveauSuivant) {
                Optional<InscriptionModule> inscription = inscriptionModuleRepository
                        .findByEtudiantAndModule(etudiantId, module.getIdModule());
                
                if (inscription.isPresent()) {
                    EtudiantDeliberationDTO.ModuleNotesDTO moduleDTO = 
                        getNotesModulePourEtudiant(etudiantId, module, anneeUniversitaire);
                    
                    if (moduleDTO != null) {
                        moduleDTO.setModuleSupplementaire(true);
                        modulesSupp.add(moduleDTO);
                    }
                }
            }
        }
        
        return modulesSupp;
    }
    
    /**
     * Calcule les moyennes générales et les rangs des étudiants
     */
    private void calculerMoyennesEtRangs(List<EtudiantDeliberationDTO> etudiants) {
        // Calcul des moyennes générales
        for (EtudiantDeliberationDTO etudiant : etudiants) {
            List<BigDecimal> moyennesModules = etudiant.getModuleNotes().values().stream()
                    .filter(module -> module.getMoyenneModule() != null)
                    .map(EtudiantDeliberationDTO.ModuleNotesDTO::getMoyenneModule)
                    .collect(Collectors.toList());
            
            if (!moyennesModules.isEmpty()) {
                BigDecimal moyenneGenerale = moyennesModules.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(new BigDecimal(moyennesModules.size()), 2, BigDecimal.ROUND_HALF_UP);
                
                etudiant.setMoyenneGenerale(moyenneGenerale);
            } else {
                etudiant.setMoyenneGenerale(BigDecimal.ZERO);
            }
        }
        
        // Calcul des rangs (tri par moyenne décroissante)
        List<EtudiantDeliberationDTO> etudiantsAvecMoyenne = etudiants.stream()
                .filter(e -> e.getMoyenneGenerale() != null && e.getMoyenneGenerale().compareTo(BigDecimal.ZERO) > 0)
                .sorted((e1, e2) -> e2.getMoyenneGenerale().compareTo(e1.getMoyenneGenerale()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < etudiantsAvecMoyenne.size(); i++) {
            etudiantsAvecMoyenne.get(i).setRang(i + 1);
        }
        
        // Mettre un rang par défaut pour les étudiants sans moyenne
        etudiants.stream()
                .filter(e -> e.getRang() == null)
                .forEach(e -> e.setRang(999)); // Rang par défaut pour les étudiants sans notes
    }
    
    /**
     * Crée le fichier Excel de délibération finale
     */
    private byte[] creerFichierExcelDeliberation(Niveau niveau, List<Module> modules, 
                                               List<EtudiantDeliberationDTO> etudiants,
                                               GenerationFichierRequestDTO request) throws IOException {
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Délibération " + niveau.getAlias());
        
        // Styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle formulaStyle = createFormulaStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        
        int rowNum = 0;
        
        // En-tête du fichier de délibération
        rowNum = creerEnteteDeliberation(sheet, niveau, request, titleStyle, rowNum);
        
        // En-têtes des colonnes complexes (tous les modules et leurs éléments)
        rowNum = creerEnteteColonnesDeliberation(sheet, modules, headerStyle, rowNum);
        
        // Données des étudiants avec formules de calcul
        rowNum = ajouterDonneesEtudiantsDeliberation(sheet, etudiants, modules, 
                                                   dataStyle, formulaStyle, rowNum);
        
        // Ajustement des colonnes
        ajusterLargeurColonnesDeliberation(sheet, modules);
        
        // Conversion en byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * Crée l'en-tête du fichier de délibération
     */
    private int creerEnteteDeliberation(Sheet sheet, Niveau niveau, GenerationFichierRequestDTO request,
                                      CellStyle titleStyle, int startRow) {
        int rowNum = startRow;
        
        // Ligne 1: Année universitaire et date de délibération
        Row row1 = sheet.createRow(rowNum++);
        row1.createCell(0).setCellValue("Année Universitaire");
        row1.createCell(1).setCellValue(request.getAnneeUniversitaire());
        row1.createCell(2).setCellValue("Date délibération");
        row1.createCell(3).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        row1.createCell(4).setCellValue("Classe");
        row1.createCell(5).setCellValue(niveau.getAlias());
        
        // Ligne vide
        sheet.createRow(rowNum++);
        
        return rowNum;
    }
    
    /**
     * Crée les en-têtes complexes pour la délibération (tous les modules)
     */
    private int creerEnteteColonnesDeliberation(Sheet sheet, List<Module> modules, CellStyle headerStyle, int startRow) {
        // Création des lignes d'en-tête (2 lignes : modules et éléments)
        Row headerRow1 = sheet.createRow(startRow);     // Ligne des modules
        Row headerRow2 = sheet.createRow(startRow + 1); // Ligne des éléments/détails
        
        int colNum = 0;
        
        // Colonnes de base
        Cell cellId1 = headerRow1.createCell(colNum);
        Cell cellId2 = headerRow2.createCell(colNum++);
        cellId1.setCellValue("ID ETUDIANT");
        cellId2.setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, colNum - 1, colNum - 1));
        
        Cell cellCne1 = headerRow1.createCell(colNum);
        Cell cellCne2 = headerRow2.createCell(colNum++);
        cellCne1.setCellValue("CNE");
        cellCne2.setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, colNum - 1, colNum - 1));
        
        Cell cellNom1 = headerRow1.createCell(colNum);
        Cell cellNom2 = headerRow2.createCell(colNum++);
        cellNom1.setCellValue("NOM");
        cellNom2.setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, colNum - 1, colNum - 1));
        
        Cell cellPrenom1 = headerRow1.createCell(colNum);
        Cell cellPrenom2 = headerRow2.createCell(colNum++);
        cellPrenom1.setCellValue("PRENOM");
        cellPrenom2.setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, colNum - 1, colNum - 1));
        
        // Colonnes pour chaque module
        for (Module module : modules) {
            List<Element> elements = elementRepository.findByModuleIdModule(module.getIdModule());
            int startColModule = colNum;
            
            // Colonnes pour les éléments du module
            for (Element element : elements) {
                headerRow2.createCell(colNum++).setCellValue(element.getTitre());
            }
            
            // Colonnes Moyenne et Validation du module
            headerRow2.createCell(colNum++).setCellValue("Moyenne");
            headerRow2.createCell(colNum++).setCellValue("Validation");
            
            // Fusion de la cellule du module sur toutes ses colonnes
            int endColModule = colNum - 1;
            Cell moduleCell = headerRow1.createCell(startColModule);
            moduleCell.setCellValue(module.getCode() + " (Nom de l'enseignant)");
            if (endColModule > startColModule) {
                sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startColModule, endColModule));
            }
        }
        
        // Colonnes finales : Moyenne générale et Rang
        Cell cellMoyGen1 = headerRow1.createCell(colNum);
        Cell cellMoyGen2 = headerRow2.createCell(colNum++);
        cellMoyGen1.setCellValue("Moyenne");
        cellMoyGen2.setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, colNum - 1, colNum - 1));
        
        Cell cellRang1 = headerRow1.createCell(colNum);
        Cell cellRang2 = headerRow2.createCell(colNum++);
        cellRang1.setCellValue("Rang");
        cellRang2.setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, colNum - 1, colNum - 1));
        
        // Application du style aux en-têtes
        for (int i = 0; i < colNum; i++) {
            if (headerRow1.getCell(i) != null) headerRow1.getCell(i).setCellStyle(headerStyle);
            if (headerRow2.getCell(i) != null) headerRow2.getCell(i).setCellStyle(headerStyle);
        }
        
        return startRow + 2;
    }
    
    /**
     * Ajoute les données des étudiants pour la délibération avec formules
     */
    private int ajouterDonneesEtudiantsDeliberation(Sheet sheet, List<EtudiantDeliberationDTO> etudiants,
                                                  List<Module> modules, CellStyle dataStyle, 
                                                  CellStyle formulaStyle, int startRow) {
        int rowNum = startRow;
        
        for (EtudiantDeliberationDTO etudiant : etudiants) {
            Row row = sheet.createRow(rowNum);
            int colNum = 0;
            
            // Données de base
            row.createCell(colNum++).setCellValue(etudiant.getEtudiantId());
            row.createCell(colNum++).setCellValue(etudiant.getCne());
            row.createCell(colNum++).setCellValue(etudiant.getNom());
            row.createCell(colNum++).setCellValue(etudiant.getPrenom());
            
            // Données pour chaque module
            List<Integer> moyenneModuleColumns = new ArrayList<>();
            
            for (Module module : modules) {
                EtudiantDeliberationDTO.ModuleNotesDTO moduleNotes = etudiant.getModuleNotes().get(module.getIdModule());
                List<Element> elements = elementRepository.findByModuleIdModule(module.getIdModule());
                
                int firstElementCol = colNum;
                
                // Notes des éléments
                for (Element element : elements) {
                    Cell cell = row.createCell(colNum++);
                    if (moduleNotes != null && moduleNotes.getElementNotes().containsKey(element.getIdElement())) {
                        cell.setCellValue(moduleNotes.getElementNotes().get(element.getIdElement()).doubleValue());
                    }
                    cell.setCellStyle(dataStyle);
                }
                
                // Moyenne du module (formule)
                Cell moyenneCell = row.createCell(colNum++);
                if (elements.size() > 0) {
                    String formule = creerFormuleMoyenne(firstElementCol, elements.size(), rowNum + 1);
                    moyenneCell.setCellFormula(formule);
                    moyenneModuleColumns.add(colNum - 1);
                } else {
                    moyenneCell.setCellValue(0);
                }
                moyenneCell.setCellStyle(formulaStyle);
                
                // Validation du module (formule)
                Cell validationCell = row.createCell(colNum++);
                String validationFormule = creerFormuleValidationModule(module, colNum - 1, rowNum + 1);
                validationCell.setCellFormula(validationFormule);
                validationCell.setCellStyle(formulaStyle);
            }
            
            // Moyenne générale (formule)
            Cell moyenneGeneraleCell = row.createCell(colNum++);
            if (!moyenneModuleColumns.isEmpty()) {
                String formuleGenerale = creerFormuleMoyenneGenerale(moyenneModuleColumns, rowNum + 1);
                moyenneGeneraleCell.setCellFormula(formuleGenerale);
            } else {
                moyenneGeneraleCell.setCellValue(0);
            }
            moyenneGeneraleCell.setCellStyle(formulaStyle);
            
            // Rang (valeur directe, pas de formule pour simplifier)
            Cell rangCell = row.createCell(colNum++);
            rangCell.setCellValue(etudiant.getRang() != null ? etudiant.getRang() : 999);
            rangCell.setCellStyle(dataStyle);
            
            rowNum++;
        }
        
        return rowNum;
    }
    
    /**
     * Crée la formule de validation pour un module spécifique
     */
    private String creerFormuleValidationModule(Module module, int moyenneCol, int rowNumber) {
        char moyenneColChar = (char) ('A' + moyenneCol - 1);
        
        // Récupération des seuils pour ce module
        if (!module.getNiveau().getFilieres().isEmpty()) {
            Optional<ParametreValidationDTO> parametres = parametreValidationService
                    .findByFiliereAndNiveau(module.getNiveau().getFilieres().get(0).getIdFiliere(), 
                                          module.getNiveau().getIdNiveau());
            
            if (parametres.isPresent()) {
                BigDecimal seuilNormal = parametres.get().getSeuilValidationNormale();
                BigDecimal seuilRattrapage = parametres.get().getSeuilValidationRattrapage();
                
                return String.format("IF(%c%d>=%s,\"V\",IF(%c%d>=%s,\"R\",\"NV\"))",
                        moyenneColChar, rowNumber, seuilNormal.toString(),
                        moyenneColChar, rowNumber, seuilRattrapage.toString());
            }
        }
        
        // Seuils par défaut
        return String.format("IF(%c%d>=12,\"V\",IF(%c%d>=8,\"R\",\"NV\"))",
                moyenneColChar, rowNumber, moyenneColChar, rowNumber);
    }
    
    /**
     * Crée la formule de moyenne générale à partir des moyennes des modules
     */
    private String creerFormuleMoyenneGenerale(List<Integer> moyenneModuleColumns, int rowNumber) {
        if (moyenneModuleColumns.isEmpty()) {
            return "0";
        }
        
        StringBuilder formule = new StringBuilder("AVERAGE(");
        for (int i = 0; i < moyenneModuleColumns.size(); i++) {
            if (i > 0) formule.append(",");
            char col = (char) ('A' + moyenneModuleColumns.get(i));
            formule.append(col).append(rowNumber);
        }
        formule.append(")");
        
        return formule.toString();
    }
    
    /**
     * Crée le style pour les titres
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * Ajuste les largeurs des colonnes pour la délibération
     */
    private void ajusterLargeurColonnesDeliberation(Sheet sheet, List<Module> modules) {
        int colNum = 0;
        
        // Colonnes de base
        sheet.setColumnWidth(colNum++, 3000); // ID
        sheet.setColumnWidth(colNum++, 4000); // CNE
        sheet.setColumnWidth(colNum++, 4000); // NOM
        sheet.setColumnWidth(colNum++, 4000); // PRENOM
        
        // Colonnes des modules
        for (Module module : modules) {
            List<Element> elements = elementRepository.findByModuleIdModule(module.getIdModule());
            
            // Colonnes des éléments
            for (int i = 0; i < elements.size(); i++) {
                sheet.setColumnWidth(colNum++, 2500);
            }
            
            // Colonnes moyenne et validation du module
            sheet.setColumnWidth(colNum++, 3000); // Moyenne
            sheet.setColumnWidth(colNum++, 3000); // Validation
        }
        
        // Colonnes finales
        sheet.setColumnWidth(colNum++, 3500); // Moyenne générale
        sheet.setColumnWidth(colNum++, 2500); // Rang
    }
}