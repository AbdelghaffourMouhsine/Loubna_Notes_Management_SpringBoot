package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.dto.*;
import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.service.ImportEtudiantService;
import com.example.gestion_de_notes.service.ImportEtudiantServiceAdvanced;
import com.example.gestion_de_notes.service.InscriptionModuleSupplementaireService;
import com.example.gestion_de_notes.service.ExcelTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/notes/import-advanced")
@PreAuthorize("hasRole('ADMIN_NOTES')")
public class ImportEtudiantAdvancedController {
    
    @Autowired
    private ImportEtudiantServiceAdvanced importServiceAdvanced;
    
    @Autowired
    private InscriptionModuleSupplementaireService inscriptionModuleSupplementaireService;
    
    @Autowired
    private ExcelTemplateService excelTemplateService;
    
    @GetMapping
    public String showImportForm(Model model) {
        return "admin/notes/import/form-advanced";
    }
    
    /**
     * Téléchargement du template Excel
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = excelTemplateService.genererTemplateImportEtudiants();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "template_import_etudiants.xlsx");
            headers.setContentLength(template.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Première étape : Validation du fichier et détection des conflits
     */
    @PostMapping("/valider")
    public String validerFichier(@RequestParam("file") MultipartFile file,
                                @RequestParam("anneeUniversitaire") String anneeUniversitaire,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier");
            return "redirect:/admin/notes/import-advanced";
        }
        
        ValidationImportDTO validation = importServiceAdvanced.validerFichier(file, anneeUniversitaire);
        
        if (!validation.isSuccess()) {
            model.addAttribute("validation", validation);
            return "admin/notes/import/form-advanced";
        }
        
        if (validation.hasConflits()) {
            // Afficher l'interface de résolution des conflits
            model.addAttribute("validation", validation);
            model.addAttribute("conflits", validation.getConflits());
            return "admin/notes/import/resoudre-conflits";
        } else {
            // Aucun conflit, procéder directement à l'import
            return "redirect:/admin/notes/import-advanced/confirmer?sessionId=" + validation.getSessionId() + 
                   "&anneeUniversitaire=" + anneeUniversitaire;
        }
    }
    
    /**
     * Deuxième étape : Résolution des conflits
     */
    @PostMapping("/resoudre-conflits")
    public String resoudreConflits(@RequestParam("sessionId") String sessionId,
                                  @RequestParam("anneeUniversitaire") String anneeUniversitaire,
                                  @ModelAttribute("conflits") List<ConflitDonneesDTO> conflits,
                                  Model model) {
        
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("anneeUniversitaire", anneeUniversitaire);
        model.addAttribute("conflitsResolus", conflits);
        
        return "admin/notes/import/confirmer";
    }
    
    /**
     * Troisième étape : Confirmation et import final
     */
    @GetMapping("/confirmer")
    public String showConfirmation(@RequestParam("sessionId") String sessionId,
                                  @RequestParam("anneeUniversitaire") String anneeUniversitaire,
                                  Model model) {
        
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("anneeUniversitaire", anneeUniversitaire);
        
        return "admin/notes/import/confirmer";
    }
    
    @PostMapping("/executer")
    public String executerImport(@RequestParam("sessionId") String sessionId,
                                @RequestParam("anneeUniversitaire") String anneeUniversitaire,
                                @RequestParam(value = "conflitsResolus", required = false) List<ConflitDonneesDTO> conflitsResolus,
                                @AuthenticationPrincipal CompteUtilisateur utilisateur,
                                RedirectAttributes redirectAttributes) {
        
        if (conflitsResolus == null) {
            conflitsResolus = List.of(); // Liste vide si aucun conflit
        }
        
        ImportEtudiantService.ImportResult result = importServiceAdvanced.importerAvecResolutionConflits(
            sessionId, conflitsResolus, utilisateur, anneeUniversitaire);
        
        if (result.isSuccess()) {
            String message = String.format("Import réussi: %d inscriptions, %d réinscriptions", 
                                          result.getNbInscriptions(), result.getNbReinscriptions());
            redirectAttributes.addFlashAttribute("success", message);
            
            if (!result.getWarnings().isEmpty()) {
                redirectAttributes.addFlashAttribute("warnings", result.getWarnings());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
            if (!result.getErrors().isEmpty()) {
                redirectAttributes.addFlashAttribute("errors", result.getErrors());
            }
        }
        
        return "redirect:/admin/notes/import-advanced";
    }
    
    /**
     * Gestion des étudiants ajournés
     */
    @GetMapping("/etudiants-ajournes")
    public String showEtudiantsAjournes(@RequestParam("anneeUniversitaire") String anneeUniversitaire,
                                       Model model) {
        
        List<EtudiantAjourneDTO> etudiantsAjournes = importServiceAdvanced.getEtudiantsAjournes(anneeUniversitaire);
        
        model.addAttribute("etudiantsAjournes", etudiantsAjournes);
        model.addAttribute("anneeUniversitaire", anneeUniversitaire);
        
        return "admin/notes/import/etudiants-ajournes";
    }
    
    /**
     * Inscription aux modules supplémentaires
     */
    @PostMapping("/inscrire-modules-supplementaires")
    public String inscrireModulesSupplementaires(@RequestParam("idEtudiant") Long idEtudiant,
                                                @RequestParam("anneeUniversitaire") String anneeUniversitaire,
                                                @RequestParam(value = "modulesSelectionnes", required = false) List<Long> modulesSelectionnes,
                                                @AuthenticationPrincipal CompteUtilisateur utilisateur,
                                                RedirectAttributes redirectAttributes) {
        
        try {
            if (modulesSelectionnes == null) {
                modulesSelectionnes = List.of(); // Liste vide si aucun module sélectionné
            }
            
            boolean success = inscriptionModuleSupplementaireService.inscrireModulesSupplementaires(
                idEtudiant, modulesSelectionnes, anneeUniversitaire, utilisateur);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    String.format("Inscription mise à jour avec succès (%d modules supplémentaires)", 
                                 modulesSelectionnes.size()));
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Erreur lors de la mise à jour de l'inscription");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la mise à jour : " + e.getMessage());
        }
        
        return "redirect:/admin/notes/import-advanced/etudiants-ajournes?anneeUniversitaire=" + anneeUniversitaire;
    }
}
