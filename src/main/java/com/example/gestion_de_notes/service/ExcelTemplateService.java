package com.example.gestion_de_notes.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelTemplateService {
    
    /**
     * Génère un template Excel pour l'import des étudiants
     */
    public byte[] genererTemplateImportEtudiants() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Template Import Etudiants");
        
        // Style pour l'en-tête
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        
        // Style pour les exemples
        CellStyle exampleStyle = workbook.createCellStyle();
        exampleStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        exampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        exampleStyle.setBorderBottom(BorderStyle.THIN);
        exampleStyle.setBorderTop(BorderStyle.THIN);
        exampleStyle.setBorderRight(BorderStyle.THIN);
        exampleStyle.setBorderLeft(BorderStyle.THIN);
        
        // Créer l'en-tête
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID_ETUDIANT", "CNE", "NOM", "PRENOM", "ID_NIVEAU_ACTUEL", "TYPE"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Ajouter des exemples
        String[][] exemples = {
            {"ID Etudiant 1", "CNE001", "ALAMI", "Ahmed", "1", "INSCRIPTION"},
            {"ID Etudiant 2", "CNE002", "BENALI", "Fatima", "2", "REINSCRIPTION"},
            {"ID Etudiant 3", "CNE003", "CHAKIR", "Mohamed", "3", "INSCRIPTION"},
            {"ID Etudiant 4", "CNE004", "DARIF", "Aicha", "1", "REINSCRIPTION"}
        };
        
        for (int i = 0; i < exemples.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < exemples[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(exemples[i][j]);
                cell.setCellStyle(exampleStyle);
            }
        }
        
        // Ajuster la largeur des colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // Ajouter un peu plus d'espace
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 1000);
        }
        
        // Ajouter une feuille d'instructions
        Sheet instructionsSheet = workbook.createSheet("Instructions");
        addInstructions(workbook, instructionsSheet);
        
        // Ajouter une feuille avec les niveaux disponibles
        Sheet niveauxSheet = workbook.createSheet("Niveaux Disponibles");
        addNiveauxDisponibles(workbook, niveauxSheet);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    private void addInstructions(Workbook workbook, Sheet sheet) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        
        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);
        
        int rowNum = 0;
        
        // Titre
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Instructions pour l'import des étudiants");
        titleCell.setCellStyle(titleStyle);
        
        rowNum++; // Ligne vide
        
        // Description des colonnes
        Row descRow = sheet.createRow(rowNum++);
        Cell descCell = descRow.createCell(0);
        descCell.setCellValue("Description des colonnes :");
        descCell.setCellStyle(boldStyle);
        
        String[][] descriptions = {
            {"ID_ETUDIANT", "Identifiant unique de l'étudiant (peut être alphanumérique)"},
            {"CNE", "Code National Étudiant (obligatoire, unique)"},
            {"NOM", "Nom de famille de l'étudiant (obligatoire)"},
            {"PRENOM", "Prénom de l'étudiant (obligatoire)"},
            {"ID_NIVEAU_ACTUEL", "Identifiant numérique du niveau (voir feuille 'Niveaux Disponibles')"},
            {"TYPE", "INSCRIPTION (nouveau) ou REINSCRIPTION (ancien étudiant)"}
        };
        
        for (String[] desc : descriptions) {
            Row row = sheet.createRow(rowNum++);
            Cell col1 = row.createCell(0);
            col1.setCellValue("• " + desc[0] + " :");
            col1.setCellStyle(boldStyle);
            
            Cell col2 = row.createCell(1);
            col2.setCellValue(desc[1]);
        }
        
        rowNum++; // Ligne vide
        
        // Règles importantes
        Row rulesRow = sheet.createRow(rowNum++);
        Cell rulesCell = rulesRow.createCell(0);
        rulesCell.setCellValue("Règles importantes :");
        rulesCell.setCellStyle(boldStyle);
        
        String[] rules = {
            "Les nouveaux étudiants (INSCRIPTION) ne doivent pas exister en base",
            "Les réinscriptions (REINSCRIPTION) doivent concerner des étudiants existants",
            "Le niveau doit être cohérent avec les résultats de l'année précédente",
            "En cas de conflit de données, vous serez invité à choisir",
            "Les étudiants ajournés ne seront inscrits qu'aux modules non validés"
        };
        
        for (String rule : rules) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue("• " + rule);
        }
        
        // Ajuster les colonnes
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 8000);
    }
    
    private void addNiveauxDisponibles(Workbook workbook, Sheet sheet) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // En-tête
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID_NIVEAU", "ALIAS", "LIBELLE", "NIVEAU_SUIVANT"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Données des niveaux (basées sur l'annexe du document)
        String[][] niveaux = {
            {"1", "CP1", "Première Année Cycle Préparatoire", "2"},
            {"2", "CP2", "Deuxième Année Cycle Préparatoire", "12"},
            {"3", "GI1", "Génie Informatique 1", "4"},
            {"4", "GI2", "Génie Informatique 2", "5"},
            {"5", "GI3", "Génie Informatique 3", ""},
            {"6", "GC1", "Génie Civil 1", "7"},
            {"7", "GC2", "Génie Civil 2", "8"},
            {"8", "GC3", "Génie Civil 3", ""},
            {"12", "C.Ing1", "Première Année Cycle Ingénieur", ""},
            {"13", "GEER1", "Génie Energétique et Energies renouvelables 1", "14"},
            {"14", "GEER2", "Génie Energétique et Energies renouvelables 2", "15"},
            {"15", "GEER3", "Génie Energétique et Energies renouvelables 3", ""},
            {"16", "GEE1", "Génie de l'eau et de l'Environnement 1", "21"},
            {"21", "GEE2", "Génie de l'eau et de l'Environnement 2", "22"},
            {"22", "GEE3", "Génie de l'eau et de l'Environnement 3", ""},
            {"23", "GM1", "Génie Mécanique 1", "24"},
            {"24", "GM2", "Génie Mécanique 2", "25"},
            {"25", "GM3", "Génie Mécanique 3", ""},
            {"26", "ID1", "Ingénierie des données 1", "27"},
            {"27", "ID2", "Ingénierie des données 2", "28"},
            {"28", "ID3", "Ingénierie des données 3", ""}
        };
        
        for (int i = 0; i < niveaux.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < niveaux[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(niveaux[i][j]);
            }
        }
        
        // Ajuster les colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 1000);
        }
    }
}
