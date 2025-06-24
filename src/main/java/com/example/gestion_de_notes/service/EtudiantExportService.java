package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.EtudiantExportDTO;
import com.example.gestion_de_notes.dto.EtudiantSearchDTO;
import com.example.gestion_de_notes.entity.Etudiant;
import com.example.gestion_de_notes.entity.Inscription;
import com.example.gestion_de_notes.repository.EtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EtudiantExportService {
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private EtudiantService etudiantService;
    
    private static final String CSV_HEADER = "CNE,Nom,Prénom,Nom Complet,Niveau Actuel,Filière,Année Universitaire,Type Inscription,Date Inscription,Nombre Notes,Moyenne Générale,Statut\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Exporte les étudiants selon les critères de recherche en format CSV
     */
    public byte[] exportEtudiantsToCSV(EtudiantSearchDTO searchDTO) throws IOException {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<Etudiant> etudiants = etudiantService.searchEtudiants(searchDTO, pageable)
            .getContent()
            .stream()
            .map(dto -> etudiantRepository.findById(dto.getIdEtudiant()).orElse(null))
            .filter(e -> e != null)
            .collect(Collectors.toList());
        
        return generateCSV(etudiants);
    }
    
    /**
     * Exporte les étudiants d'une classe spécifique
     */
    public byte[] exportClasseToCSV(Long niveauId, String anneeUniversitaire) throws IOException {
        List<Etudiant> etudiants = etudiantRepository.findByNiveauAndAnnee(niveauId, anneeUniversitaire);
        return generateCSV(etudiants);
    }
    
    /**
     * Exporte une sélection d'étudiants par leurs IDs
     */
    public byte[] exportSelectedEtudiantsToCSV(List<Long> etudiantIds) throws IOException {
        List<Etudiant> etudiants = etudiantRepository.findByIdIn(etudiantIds);
        return generateCSV(etudiants);
    }
    
    /**
     * Génère le fichier CSV à partir d'une liste d'étudiants
     */
    private byte[] generateCSV(List<Etudiant> etudiants) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            // Ajout du BOM UTF-8 pour Excel
            writer.write('\ufeff');
            
            // En-tête
            writer.write(CSV_HEADER);
            
            // Données
            for (Etudiant etudiant : etudiants) {
                EtudiantExportDTO exportDTO = convertToExportDTO(etudiant);
                writer.write(formatCSVLine(exportDTO));
            }
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Convertit un étudiant en DTO d'export
     */
    private EtudiantExportDTO convertToExportDTO(Etudiant etudiant) {
        EtudiantExportDTO dto = new EtudiantExportDTO();
        
        dto.setCne(etudiant.getCne());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setNomComplet(etudiant.getNomComplet());
        
        // Inscription la plus récente
        if (!etudiant.getInscriptions().isEmpty()) {
            Inscription inscriptionActuelle = etudiant.getInscriptions().stream()
                .reduce((i1, i2) -> i1.getDateInscription().isAfter(i2.getDateInscription()) ? i1 : i2)
                .orElse(null);
            
            if (inscriptionActuelle != null) {
                dto.setNiveauActuel(inscriptionActuelle.getNiveau().getAlias());
                dto.setAnneeUniversitaire(inscriptionActuelle.getAnneeUniversitaire());
                dto.setTypeInscription(inscriptionActuelle.getTypeInscription().name());
                dto.setDateInscription(inscriptionActuelle.getDateInscription().format(DATE_FORMATTER));
                
                // Filière (première filière du niveau)
                if (!inscriptionActuelle.getNiveau().getFilieres().isEmpty()) {
                    dto.setFiliere(inscriptionActuelle.getNiveau().getFilieres().get(0).getAlias());
                }
            }
        }
        
        // Statistiques des notes
        if (!etudiant.getNotes().isEmpty()) {
            dto.setNombreNotes(etudiant.getNotes().size());
            
            // Calcul de la moyenne générale (simplifié)
            double moyenneGenerale = etudiant.getNotes().stream()
                .mapToDouble(note -> note.getValeurNote() != null ? note.getValeurNote().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
            
            dto.setMoyenneGenerale(String.format("%.2f", moyenneGenerale));
        } else {
            dto.setNombreNotes(0);
            dto.setMoyenneGenerale("N/A");
        }
        
        // Statut (logique simplifiée)
        dto.setStatut(determinerStatut(etudiant));
        
        return dto;
    }
    
    /**
     * Détermine le statut de l'étudiant
     */
    private String determinerStatut(Etudiant etudiant) {
        if (etudiant.getInscriptions().isEmpty()) {
            return "Non inscrit";
        }
        
        // Logique simplifiée - à enrichir selon les règles métier
        Inscription derniereInscription = etudiant.getInscriptions().stream()
            .reduce((i1, i2) -> i1.getDateInscription().isAfter(i2.getDateInscription()) ? i1 : i2)
            .orElse(null);
        
        if (derniereInscription != null) {
            switch (derniereInscription.getTypeInscription()) {
                case INSCRIPTION:
                    return "Nouveau";
                case REINSCRIPTION:
                    return "Réinscrit";
                default:
                    return "Actif";
            }
        }
        
        return "Inconnu";
    }
    
    /**
     * Formate une ligne CSV
     */
    private String formatCSVLine(EtudiantExportDTO dto) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
            escapeCSV(dto.getCne()),
            escapeCSV(dto.getNom()),
            escapeCSV(dto.getPrenom()),
            escapeCSV(dto.getNomComplet()),
            escapeCSV(dto.getNiveauActuel()),
            escapeCSV(dto.getFiliere()),
            escapeCSV(dto.getAnneeUniversitaire()),
            escapeCSV(dto.getTypeInscription()),
            escapeCSV(dto.getDateInscription()),
            dto.getNombreNotes() != null ? dto.getNombreNotes().toString() : "0",
            escapeCSV(dto.getMoyenneGenerale()),
            escapeCSV(dto.getStatut())
        );
    }
    
    /**
     * Échappe les caractères spéciaux pour CSV
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Génère un nom de fichier pour l'export
     */
    public String generateFileName(String prefix) {
        return String.format("%s_%s.csv", 
            prefix,
            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        );
    }
}
