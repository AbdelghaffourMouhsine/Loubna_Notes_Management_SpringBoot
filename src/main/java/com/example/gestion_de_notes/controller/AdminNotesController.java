package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.dto.GenerationFichierRequestDTO;
import com.example.gestion_de_notes.service.GenerationFichierNotesService;
import com.example.gestion_de_notes.service.ModuleService;
import com.example.gestion_de_notes.service.NiveauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/notes")
@PreAuthorize("hasRole('ADMIN_NOTES')")
public class AdminNotesController {
    
    @Autowired
    private GenerationFichierNotesService generationFichierNotesService;
    
    @Autowired
    private ModuleService moduleService;
    
    @Autowired
    private NiveauService niveauService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "Bienvenue dans l'administration des notes");
        model.addAttribute("userRole", "ADMIN_NOTES");
        return "admin/notes/dashboard";
    }
    
    // ===============================
    // GÉNÉRATION DES FICHIERS DE NOTES
    // ===============================
    
    @GetMapping("/fichiers")
    public String fichiers(Model model) {
        model.addAttribute("modules", moduleService.findAll());
        model.addAttribute("niveaux", niveauService.findAll());
        model.addAttribute("request", new GenerationFichierRequestDTO());
        return "admin/notes/fichiers/index";
    }
    
    @PostMapping("/fichiers/module")
    public ResponseEntity<byte[]> genererFichierModule(@ModelAttribute GenerationFichierRequestDTO request,
                                                      RedirectAttributes redirectAttributes) {
        try {
            // Validation des données
            if (request.getModuleId() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Définir les valeurs par défaut si nécessaires
            if (request.getAnneeUniversitaire() == null || request.getAnneeUniversitaire().isEmpty()) {
                request.setAnneeUniversitaire("2024/2025");
            }
            if (request.getSemestre() == null || request.getSemestre().isEmpty()) {
                request.setSemestre("Automne");
            }
            if (request.getSession() == null || request.getSession().isEmpty()) {
                request.setSession("NORMALE");
            }
            
            byte[] fichierExcel = generationFichierNotesService.genererFichierNotesModule(request);
            
            String nomFichier = String.format("Notes_Module_%d_%s_%s.xlsx", 
                request.getModuleId(), request.getSession(), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", nomFichier);
            
            return new ResponseEntity<>(fichierExcel, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/fichiers/deliberation")
    public ResponseEntity<byte[]> genererFichierDeliberation(@ModelAttribute GenerationFichierRequestDTO request,
                                                            RedirectAttributes redirectAttributes) {
        try {
            // Validation des données
            if (request.getNiveauId() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Définir les valeurs par défaut si nécessaires
            if (request.getAnneeUniversitaire() == null || request.getAnneeUniversitaire().isEmpty()) {
                request.setAnneeUniversitaire("2024/2025");
            }
            
            byte[] fichierExcel = generationFichierNotesService.genererFichierDeliberationNiveau(request);
            
            String nomFichier = String.format("Deliberation_Niveau_%d_%s.xlsx", 
                request.getNiveauId(), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", nomFichier);
            
            return new ResponseEntity<>(fichierExcel, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
